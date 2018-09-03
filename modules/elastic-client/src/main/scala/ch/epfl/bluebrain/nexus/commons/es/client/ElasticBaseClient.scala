package ch.epfl.bluebrain.nexus.commons.es.client

import akka.http.scaladsl.model.{HttpRequest, StatusCode}
import cats.MonadError
import cats.syntax.flatMap._
import cats.syntax.functor._
import ch.epfl.bluebrain.nexus.commons.es.client.ElasticBaseClient._
import ch.epfl.bluebrain.nexus.commons.http.HttpClient.UntypedHttpClient
import journal.Logger

/**
  * ElasticBaseClient provides the common methods and vals used for elastic clients.
  *
  * @tparam F the monadic effect type
  */
abstract class ElasticBaseClient[F[_]](implicit
                                       cl: UntypedHttpClient[F],
                                       F: MonadError[F, Throwable]) {
  private[client] val log = Logger[this.type]

  private[client] def execute(req: HttpRequest, expectedCodes: Set[StatusCode], intent: String): F[Unit] =
    execute(req, expectedCodes, Set.empty, intent).map(_ => ())

  private[client] def execute(req: HttpRequest,
                              expectedCodes: Set[StatusCode],
                              ignoredCodes: Set[StatusCode],
                              intent: String): F[Boolean] =
    cl(req).flatMap { resp =>
      if (expectedCodes.contains(resp.status)) cl.discardBytes(resp.entity).map(_ => true)
      else if (ignoredCodes.contains(resp.status)) cl.discardBytes(resp.entity).map(_ => false)
      else
        ElasticFailure.fromResponse(resp).flatMap { f =>
          log.error(
            s"Unexpected ElasticSearch response for intent '$intent':\nRequest: '${req.method} ${req.uri}'\nStatus: '${resp.status}'\nResponse: '${f.body}'")
          F.raiseError(f)
        }
    }

  private[client] def indexPath(indices: Set[String]) =
    if (indices.isEmpty) anyIndexPath
    else indices.mkString(",")
}

object ElasticBaseClient {
  private[client] val source            = "_source"
  private[client] val anyIndexPath      = "_all"
  private[client] val ignoreUnavailable = "ignore_unavailable"
  private[client] val allowNoIndices    = "allow_no_indices"
}
