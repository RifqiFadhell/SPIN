package com.spin.id.ui.home.shop.detail

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import com.google.android.material.textfield.TextInputLayout
import com.spin.id.R

class TextInput @JvmOverloads constructor(
    context: Context,
    attrSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrSet, defStyleAttr), FieldInput {

    override var key: String = ""
    override var text: String = ""
    override var type: String = ""
    override var required: Boolean = true

    private val textInput: EditText
    private val textInputEdit: TextInputLayout

    init {
        val view = inflate(context, R.layout.text_input, this)
        textInput = view.findViewById(R.id.editText)
        textInputEdit = view.findViewById(R.id.textInput)

        //This is necessary to avoid having the same component twice on the screen with the same id.
        textInput.id = View.generateViewId()
    }

    override fun getValue(): String {
        return textInput.text.toString()
    }

    override fun setHint(hint: String) {
        textInputEdit.hint = hint
    }

    override fun setInputType(type: String) {
        when (type) {
            "text" -> textInput.inputType = InputType.TYPE_CLASS_TEXT
            "number" -> textInput.inputType = InputType.TYPE_CLASS_NUMBER
        }
    }

    override fun setError(value: String) {
        if (value.isEmpty()) {
            textInputEdit.error = "Isian form tidak boleh kosong!"
        } else {
            textInput.setText(value)
            textInputEdit.error = null
        }
    }
}