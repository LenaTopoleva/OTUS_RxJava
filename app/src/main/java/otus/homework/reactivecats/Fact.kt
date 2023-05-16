package otus.homework.reactivecats

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Fact(
    @field:SerializedName("text")
    val text: String
) : Serializable