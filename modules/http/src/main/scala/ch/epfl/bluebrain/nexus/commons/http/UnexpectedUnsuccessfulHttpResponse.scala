package ch.epfl.bluebrain.nexus.commons.http

import akka.http.scaladsl.model.HttpResponse
import ch.epfl.bluebrain.nexus.commons.types.Err

/**
  * Error type representing an unexpected unsuccessful http response.  Its entity bytes are discarded.
  *
  * @param response the underlying unexpected http response
  */
@SuppressWarnings(Array("IncorrectlyNamedExceptions"))
final case class UnexpectedUnsuccessfulHttpResponse(response: HttpResponse, body: String)
    extends Err("Received an unexpected http response while communicating with an external service")
