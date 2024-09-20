package mk.ukim.finki.dnick.hosting.webdav

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
interface WebdavService {

    fun upload(name: String, path: String, file: MultipartFile): String
}