import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContactsData(
    val contactsId: Int,
    val name: String,
    val number: String
) : Parcelable
