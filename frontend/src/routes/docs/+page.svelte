<script lang="ts">
  import { onMount } from 'svelte';

  type TocItem = {
    id: string;
    title: string;
    children?: TocItem[];
  };

  const toc: TocItem[] = [
    { id: 'overview', title: 'Overview' },
    {
      id: 'java-apps',
      title: 'Java Applications'
    },
    {
      id: 'configuration',
      title: 'Configuration',
      children: [
        { id: 'gradle', title: 'Gradle projects' },
        { id: 'maven', title: 'Maven projects' },
        { id: 'spring-profiles', title: 'Spring profiles' },
        { id: 'env-vars', title: 'Environment variables' }
      ]
    },
    { id: 'networking', title: 'Services & Ingress' },
    { id: 'examples', title: 'Examples' },
    { id: 'faq', title: 'FAQ' }
  ];

  let activeId: string = toc[0].id;

  const allSectionIds = [...toc.map(t => t.id), ...toc.flatMap(t => t.children?.map(c => c.id) ?? [])];

  onMount(() => {
    const observer = new IntersectionObserver(
      entries => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            activeId = entry.target.id;
          }
        }
      },
      {
        root: null,
        rootMargin: '0px 0px -70% 0px',
        threshold: 0.1
      }
    );

    const elements = allSectionIds.map(id => document.getElementById(id)).filter(Boolean) as Element[];
    elements.forEach(el => observer.observe(el));
    return () => observer.disconnect();
  });
</script>

<div class="layout">
  <nav aria-label="Documentation Table of Contents" class="-mt-[3.625rem] box-border px-2 pt-[3.625rem]">
    <ul class="toc">
      {#each toc as item}
        <li>
          <a
            href={`#${item.id}`}
            on:click|stopPropagation={() => (activeId = item.id)}
            class:active={activeId === item.id}>{item.title}</a>
          {#if item.children?.length}
            <ul class="toc child">
              {#each item.children as child}
                <li>
                  <a
                    href={`#${child.id}`}
                    on:click|stopPropagation={() => (activeId = child.id)}
                    class:active={activeId === child.id}>
                    {child.title}
                  </a>
                </li>
              {/each}
            </ul>
          {/if}
        </li>
      {/each}
    </ul>
  </nav>

  <main class="-mt-[3.625rem] box-border px-8 pt-[3.625rem] pb-8 h-screen overflow-auto">
    <h1>Seaport Hosting Documentation</h1>

    <section id="overview">
      <h2>Overview</h2>
      <p>
        Seaport Hosting streamlines building, deploying and exposing containerized applications to a local Kubernetes
        cluster. It integrates a local container registry, WebDAV storage, Kaniko-based image builds and simple controls
        for Services and Ingress.
      </p>
      <div class="note">
        The backend runs in Kotlin/Spring, but this page describes client-side usage and app configuration.
      </div>
    </section>

    <section id="java-apps">
      <h2>Java Applications</h2>
      <p>
        Java apps (including Spring Boot) work best as an executable JAR. Seaport provides base images and templates for
        building and running such apps. You push sources or Docker context to storage, Seaport triggers a Kaniko build
        into the internal registry, and then you deploy the image to the cluster.
      </p>
      <p>Key points for Java apps:</p>
      <ul>
        <li>Produce a single runnable JAR (fat/uber JAR).</li>
        <li>Allow the runtime port to be configured via <code>PORT</code>.</li>
        <li>Prefer non-root user execution where possible.</li>
        <li>Expose HTTP readiness/liveness endpoints for Kubernetes probes.</li>
      </ul>
    </section>

    <section id="configuration">
      <h2>Configuration</h2>

      <section id="gradle">
        <h3>Gradle projects</h3>
        <p>Typical Spring Boot build with Gradle:</p>
        <pre>{`# build
./gradlew clean bootJar

# resulting artifact (example)
build/libs/my-app-0.0.1-SNAPSHOT.jar`}</pre>
        <p>Recommended application properties to allow dynamic port/context path:</p>
        <pre>{`# src/main/resources/application.yaml
server:
  port: 
    # Allow Seaport/Kubernetes to override via env PORT
    # Fallback to 8080 locally
    \${PORT:8080}
  servlet:
    context-path: \${CONTEXT_PATH:/}`}</pre>
      </section>

      <section id="maven">
        <h3>Maven projects</h3>
        <p>Typical Spring Boot build with Maven:</p>
        <pre>{`# build
./mvnw -DskipTests package

# resulting artifact (example)
target/my-app-0.0.1-SNAPSHOT.jar`}</pre>
        <p>
          Use the same dynamic <code>server.port</code> and <code>server.servlet.context-path</code> approach as above.
        </p>
      </section>

      <section id="spring-profiles">
        <h3>Spring profiles</h3>
        <p>Seaport commonly runs apps in one of two profiles:</p>
        <ul>
          <li><code>external</code>: running outside the cluster, talking to Kubernetes via a kubeconfig.</li>
          <li><code>cluster</code>: running inside the cluster with service account RBAC.</li>
        </ul>
        <p>
          For local dev with Seaport, keep your app profile flexible. You can control it via
          <code>SPRING_PROFILES_ACTIVE</code> environment variable.
        </p>
        <pre>{`# Example runtime overrides
SPRING_PROFILES_ACTIVE=cluster
JAVA_TOOL_OPTIONS="-Duser.timezone=UTC"`}</pre>
      </section>

      <section id="env-vars">
        <h3>Environment variables</h3>
        <div class="grid-2">
          <div>
            <h4>Common</h4>
            <ul>
              <li><code>PORT</code>: container HTTP port your app listens on.</li>
              <li><code>CONTEXT_PATH</code>: servlet context path (default <code>/</code>).</li>
              <li><code>JAVA_OPTS</code>/<code>JAVA_TOOL_OPTIONS</code>: JVM flags.</li>
            </ul>
          </div>
          <div>
            <h4>Spring</h4>
            <ul>
              <li>
                <code>SPRING_PROFILES_ACTIVE</code>: profile selection (<code>cluster</code> / <code>external</code> / custom).
              </li>
              <li>
                <code>SPRING_DATASOURCE_URL</code>, <code>SPRING_DATASOURCE_USERNAME</code>,
                <code>SPRING_DATASOURCE_PASSWORD</code> for DB.
              </li>
            </ul>
          </div>
        </div>
      </section>
    </section>

    <section id="networking">
      <h2>Services & Ingress</h2>
      <p>In Seaport, networking follows Kubernetes primitives:</p>
      <ul>
        <li>
          <strong>Service</strong>: Gives your Pod(s) a stable virtual IP in-cluster. You map the Service to the port
          your container exposes (e.g., 8080).
        </li>
        <li>
          <strong>Ingress</strong>: Exposes HTTP(S) routes from outside the cluster to a Service. You configure
          hostnames and paths.
        </li>
      </ul>
      <div class="note">
        Configuring a Service in Seaport means selecting the target container port to expose and (optionally) the
        Service type. Configuring an Ingress means specifying one or more hostnames (e.g. <code>app.local</code>) and
        path rules that route to that Service.
      </div>
      <p>Illustrative Kubernetes snippets the platform effectively manages for you:</p>
      <pre>{`# Service (ClusterIP)
apiVersion: v1
kind: Service
metadata:
  name: my-app
spec:
  selector:
    app: my-app
  ports:
    - port: 80
      targetPort: 8080

---
# Ingress
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: my-app
spec:
  rules:
    - host: my-app.local
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: my-app
                port:
                  number: 80`}</pre>
      <p>
        Ensure your application actually listens on <code>PORT</code> and responds under the configured
        <code>CONTEXT_PATH</code> so the Service and Ingress route correctly.
      </p>
    </section>

    <section id="examples">
      <h2>Examples</h2>
      <h3>Spring Boot (Gradle)</h3>
      <pre>{`# build
./gradlew clean bootJar

# run locally
PORT=8080 java -jar build/libs/app.jar`}</pre>
      <pre>{`# src/main/resources/application.yaml
server:
  port: \${PORT:8080}
  servlet:
    context-path: \${CONTEXT_PATH:/}

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      probes:
        enabled: true`}</pre>
      <p>With the above, Kubernetes health probes and Ingress routing work smoothly.</p>
    </section>

    <section id="faq">
      <h2>FAQ</h2>
      <h3>Which port should I expose?</h3>
      <p>
        Expose the port your app listens on. With Spring Boot + <code>server.port</code>, this is typically
        <code>8080</code>.
      </p>
      <h3>How does Ingress hostname resolve locally?</h3>
      <p>
        The local cluster is wired for name resolution via the provided scripts (see the repository’s <code
          >cloud/local/cluster</code>
        configs). You may need a hosts file entry for <code>*.local</code> domains depending on your setup.
      </p>
      <h3>Do I need Docker locally?</h3>
      <p>
        Seaport uses Kaniko inside the cluster to build images; you generally do not need Docker on the host for builds
        initiated via the platform.
      </p>
    </section>
  </main>
</div>

<style>
  :global(html) {
    scroll-behavior: smooth;
  }

  .layout {
    display: grid;
    grid-template-columns: 260px 1fr;
    gap: 1.5rem;
  }

  @media (max-width: 960px) {
    .layout {
      grid-template-columns: 1fr;
    }
    nav {
      position: static;
      top: auto;
      max-height: none;
      border-right: none;
      border-bottom: 1px solid var(--border, #e5e7eb);
      padding-bottom: 0.75rem;
      margin-bottom: 1rem;
    }
  }

  nav {
    position: sticky;
    top: 1rem;
    align-self: start;
    max-height: calc(100dvh - 2rem);
    overflow: auto;
    padding-right: 0.5rem;
  }

  .toc {
    list-style: none;
    margin: 0;
    padding: 0;
  }

  .toc a {
    display: block;
    padding: 0.35rem 0.25rem;
    color: var(--text, #111827);
    text-decoration: none;
    border-radius: 0.375rem;
  }

  .toc a.active {
    background: var(--active-bg, #eef2ff);
    color: var(--active-text, #3730a3);
    font-weight: 600;
  }

  .toc .child {
    margin-left: 0.75rem;
    border-left: 2px solid var(--border, #e5e7eb);
    padding-left: 0.75rem;
  }

  main section {
    padding-top: 0.25rem;
    padding-bottom: 1rem;
    margin-bottom: 1.25rem;
    border-bottom: 1px dashed var(--border, #e5e7eb);
  }

  h1 {
    margin-top: 0.25rem;
    margin-bottom: 1rem;
    font-size: 1.875rem;
    line-height: 2.25rem;
  }

  h2 {
    margin: 0.75rem 0 0.5rem;
    font-size: 1.25rem;
  }

  h3 {
    margin: 0.5rem 0 0.25rem;
    font-size: 1.05rem;
  }

  p {
    margin: 0.35rem 0 0.75rem;
    color: #374151;
  }

  code,
  pre {
    font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
  }

  pre {
    background: #0b1020;
    color: #e5e7eb;
    padding: 0.75rem;
    border-radius: 0.5rem;
    overflow-x: auto;
    margin: 0.5rem 0 1rem;
  }

  .note {
    background: #f8fafc;
    border: 1px solid #e5e7eb;
    border-left: 4px solid #3b82f6;
    padding: 0.75rem;
    border-radius: 0.25rem;
    margin: 0.75rem 0 1rem;
  }

  .grid-2 {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
  }

  @media (max-width: 960px) {
    .grid-2 {
      grid-template-columns: 1fr;
    }
  }
</style>
