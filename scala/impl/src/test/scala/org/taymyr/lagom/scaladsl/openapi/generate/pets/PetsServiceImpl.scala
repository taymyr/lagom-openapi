package org.taymyr.lagom.scaladsl.openapi.generate.pets

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.typesafe.config.Config
import org.taymyr.lagom.scaladsl.openapi.OpenAPIServiceImpl

class PetsServiceImpl(override val config: Config) extends PetsService with OpenAPIServiceImpl {
  override def find(tags: List[String], limit: Option[Int]): ServiceCall[NotUsed, List[Pet]] = ???

  override def create: ServiceCall[NewPet, Pet] = ???

  override def findBy(id: Long): ServiceCall[NotUsed, Pet] = ???

  override def delete(id: Long): ServiceCall[NotUsed, NotUsed] = ???
}
