package com.sygic.driving.testapp.data.driving.remote.utils

import com.sygic.driving.testapp.core.utils.Constants.ISO_8601_FORMAT
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class QueryConverterFactory private constructor(): Converter.Factory()  {
    companion object {
        fun create(): QueryConverterFactory = QueryConverterFactory()
    }

    override fun stringConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, String>? {
        return if(type == Date::class.java)
                DateQueryConverter
            else
                super.stringConverter(type, annotations, retrofit)
    }

    object DateQueryConverter: Converter<Date, String> {

        private val dateFormat = SimpleDateFormat(ISO_8601_FORMAT, Locale.US)

        override fun convert(value: Date): String? {
            return dateFormat.format(value)
        }

    }
}