package org.taymyr.lagom.scaladsl.openapi

import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

import akka.NotUsed
import com.google.common.reflect.TypeToken
import com.lightbend.lagom.scaladsl.api.Descriptor._
import com.lightbend.lagom.scaladsl.api.ServiceSupport.ScalaMethodServiceCall
import com.lightbend.lagom.scaladsl.api.Service
import com.lightbend.lagom.scaladsl.api.ServiceCall
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem.HttpMethod
import org.taymyr.lagom.internal.openapi.LagomCallInfo
import org.taymyr.lagom.internal.openapi.LagomServiceInfo
import org.taymyr.lagom.internal.openapi.{ SpecGenerator => InternalServiceReader }

import scala.collection.JavaConverters._

class SpecGenerator(spec: OpenAPI = new OpenAPI()) extends InternalServiceReader(spec) {

  protected def opeapiPath(call: Call[_, _]): String = call.callId match {
    case restCallId: RestCallId   => openapiPath(restCallId.pathPattern)
    case pathCallId: PathCallId   => openapiPath(pathCallId.pathPattern)
    case namedCallId: NamedCallId => s"/${namedCallId.name}"
    case other                    => throw new IllegalArgumentException(s"${other.getClass} is not supported")
  }

  protected def httpMethod(call: Call[_, _], method: Method): HttpMethod = call.callId match {
    case callId: RestCallId => HttpMethod.valueOf(callId.method.name)
    case _                  => if (hasRequestBody(method)) HttpMethod.POST else HttpMethod.GET
  }

  protected def hasRequestBody(method: Method): Boolean = {
    val serviceCallType = TypeToken
      .of(method.getGenericReturnType)
      .asInstanceOf[TypeToken[ServiceCall[_, _]]]
      .getSupertype(classOf[ServiceCall[_, _]])
      .getType
      .asInstanceOf[ParameterizedType]
    if (serviceCallType == null) throw new IllegalStateException("ServiceCall is not a parameterized type?")
    val arguments = serviceCallType.getActualTypeArguments
    if (arguments.length != 2) throw new IllegalStateException("ServiceCall does not have 2 type arguments?")
    if (method.getReturnType != classOf[ServiceCall[_, _]])
      throw new IllegalArgumentException("Service calls must return ServiceCall, subtypes are not allowed")
    arguments(0) != classOf[NotUsed]
  }

  private def parseServiceInfo(service: Service): LagomServiceInfo = {
    val serviceClass = service.getClass
    val calls: List[LagomCallInfo] = service.descriptor.calls
      .map(call =>
        call.serviceCallHolder match {
          case serviceCall: ScalaMethodServiceCall[_, _] =>
            val method = serviceCall.method
            new LagomCallInfo(method, opeapiPath(call), httpMethod(call, method).name())
          case _ =>
            throw new IllegalArgumentException(
              "Undefined type of ServiceCallHolder, only ScalaMethodServiceCall is supported at the moment"
            )
        }
      )
      .toList
    new LagomServiceInfo(serviceClass, calls.asJava)
  }

  def generate(service: Service): OpenAPI = generate(parseServiceInfo(service))

}
