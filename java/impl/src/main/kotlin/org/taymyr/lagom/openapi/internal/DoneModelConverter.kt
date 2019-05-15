package org.taymyr.lagom.openapi.internal

import akka.Done
import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.core.converter.ModelConverter
import io.swagger.v3.core.converter.ModelConverterContext
import io.swagger.v3.oas.models.media.BooleanSchema
import io.swagger.v3.oas.models.media.Schema

private val doneSchema = Schema<Done>().name("Done").type("object")
    .addProperties("done", BooleanSchema()._default(true))!!

internal class DoneModelConverter : ModelConverter {
    override fun resolve(type: AnnotatedType, context: ModelConverterContext?, converters: MutableIterator<ModelConverter>): Schema<*>? =
        when {
            (type.type as? Class<*>)?.isAssignableFrom(Done::class.java) == true -> doneSchema
            converters.hasNext() -> converters.next().resolve(type, context, converters)
            else -> null
        }
}
