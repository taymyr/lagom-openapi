package org.taymyr.lagom.openapi

import akka.Done
import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.core.converter.ModelConverter
import io.swagger.v3.core.converter.ModelConverterContext
import io.swagger.v3.oas.models.media.BooleanSchema
import io.swagger.v3.oas.models.media.Schema

class DoneModelConverter : ModelConverter {
    private val doneSchema = Schema<Done>().name("Done").type("object")
        .addProperties("done", BooleanSchema()._default(true))!!
    override fun resolve(type: AnnotatedType, context: ModelConverterContext?, converters: MutableIterator<ModelConverter>): Schema<*>? {
        return if (type.type is Class<*> && (type.type as Class<*>).isAssignableFrom(Done::class.java)) {
            doneSchema
        } else if (converters.hasNext()) {
            converters.next().resolve(type, context, converters)
        } else {
            null
        }
    }
}
