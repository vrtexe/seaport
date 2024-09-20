package mk.ukim.finki.dnick.hosting.webdav

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
internal class WebdavServiceImpl(val webdavClient: WebDavClient) : WebdavService {

    override fun upload(name: String, path: String, file: MultipartFile): String {
        return webdavClient.upload(name, path, file)
    }

}