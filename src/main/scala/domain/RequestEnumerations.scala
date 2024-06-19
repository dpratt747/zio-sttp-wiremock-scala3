package domain

import sttp.model.Uri
import sttp.client3.*

enum RequestEnumerations (url: Uri) {
  val uri: Uri = url
  case Localhost extends RequestEnumerations(uri"http://localhost:8080")
}
