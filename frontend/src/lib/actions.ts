import type { ActionReturn } from 'svelte/action';

const IntoViewEvent = {
  EnterIntoView: 'enterIntoView',
  ExitOutOfView: 'exitOutOfView'
} as const;

type ActionEvent<E> = CustomEvent & {
  currentTarget: (E & EventTarget) | null;
};
interface Attributes<T> {
  'on:enterIntoView': (e: ActionEvent<T>) => unknown;
  'on:exitOutOfView': (e: ActionEvent<T>) => unknown;
}

interface Params {
  top?: string;
  bottom?: string;
  root?: Element | Document;
}

/** @type {import('svelte/action').Action}  */
export function intoView<Node extends HTMLElement>(
  node: Node,
  params: Params = {}
): ActionReturn<Params, Attributes<Node>> {
  let observer: IntersectionObserver | undefined;

  const handleIntersect: IntersectionObserverCallback = ([entry]) => {
    node.dispatchEvent(
      new CustomEvent(entry.isIntersecting ? IntoViewEvent.EnterIntoView : IntoViewEvent.ExitOutOfView)
    );
  };

  const setObserver = ({ root, top, bottom }: Params) => {
    observer?.disconnect();
    observer = new IntersectionObserver(handleIntersect, {
      root,
      rootMargin: `${top ?? '0px'} 0px ${bottom ?? '0px'} 0px`
    });
    observer.observe(node);
  };

  setObserver(params);

  return {
    update: setObserver,
    destroy() {
      observer?.disconnect();
    }
  };
}
