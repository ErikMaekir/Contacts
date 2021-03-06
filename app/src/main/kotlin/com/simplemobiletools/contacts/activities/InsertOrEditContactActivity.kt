package com.simplemobiletools.contacts.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.getColoredDrawableWithColor
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.commons.helpers.PERMISSION_READ_CONTACTS
import com.simplemobiletools.contacts.R
import com.simplemobiletools.contacts.adapters.ContactsAdapter
import com.simplemobiletools.contacts.extensions.config
import com.simplemobiletools.contacts.extensions.getContactPublicUri
import com.simplemobiletools.contacts.helpers.ADD_NEW_CONTACT_NUMBER
import com.simplemobiletools.contacts.helpers.ContactsHelper
import com.simplemobiletools.contacts.helpers.KEY_PHONE
import com.simplemobiletools.contacts.helpers.LOCATION_INSERT_OR_EDIT
import com.simplemobiletools.contacts.models.Contact
import kotlinx.android.synthetic.main.activity_insert_edit_contact.*

class InsertOrEditContactActivity : SimpleActivity() {
    private val START_INSERT_ACTIVITY = 1
    private val START_EDIT_ACTIVITY = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insert_edit_contact)
        title = getString(R.string.select_contact)
        setupViews()

        handlePermission(PERMISSION_READ_CONTACTS) {
            // we do not really care about the permission request result. Even if it was denied, load private contacts
            ContactsHelper(this).getContacts {
                gotContacts(it)
            }
        }
    }

    private fun setupViews() {
        updateTextColors(insert_edit_contact_holder)
        new_contact_tmb.setImageDrawable(resources.getColoredDrawableWithColor(R.drawable.ic_new_contact, config.textColor))
        new_contact_holder.setOnClickListener {
            Intent().apply {
                action = Intent.ACTION_INSERT
                data = ContactsContract.Contacts.CONTENT_URI
                putExtra(KEY_PHONE, intent.getStringExtra(KEY_PHONE))
                if (resolveActivity(packageManager) != null) {
                    startActivityForResult(this, START_INSERT_ACTIVITY)
                } else {
                    toast(R.string.no_app_found)
                }
            }
        }

        existing_contact_label.setTextColor(getAdjustedPrimaryColor())
    }

    private fun gotContacts(contacts: ArrayList<Contact>) {
        Contact.sorting = config.sorting
        Contact.startWithSurname = config.startNameWithSurname
        contacts.sort()

        ContactsAdapter(this, contacts, null, LOCATION_INSERT_OR_EDIT, null, existing_contact_list, existing_contact_fastscroller) {
            Intent(applicationContext, EditContactActivity::class.java).apply {
                data = getContactPublicUri(it as Contact)
                action = ADD_NEW_CONTACT_NUMBER
                putExtra(KEY_PHONE, intent.getStringExtra(KEY_PHONE))
                startActivityForResult(this, START_EDIT_ACTIVITY)
            }
        }.apply {
            addVerticalDividers(true)
            existing_contact_list.adapter = this
        }

        existing_contact_fastscroller.setScrollToY(0)
        existing_contact_fastscroller.setViews(existing_contact_list) {
            val item = (existing_contact_list.adapter as ContactsAdapter).contactItems.getOrNull(it)
            existing_contact_fastscroller.updateBubbleText(item?.getBubbleText() ?: "")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (resultCode == Activity.RESULT_OK) {
            finish()
        }
    }
}
