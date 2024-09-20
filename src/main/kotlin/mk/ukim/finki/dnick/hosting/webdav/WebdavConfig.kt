package mk.ukim.finki.dnick.hosting.webdav

import com.github.sardine.SardineFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class WebdavConfig {

    @Bean
    fun sardineClient() = SardineFactory.begin()
}