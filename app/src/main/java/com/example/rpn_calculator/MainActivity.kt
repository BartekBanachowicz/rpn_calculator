package com.example.rpn_calculator

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.example.rpn_calculator.databinding.ActivityMainBinding
import java.lang.Exception
import java.util.*
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.sqrt
import kotlinx.android.synthetic.main.activity_main.*
import java.nio.file.spi.FileTypeDetector
import kotlin.collections.ArrayList
import kotlin.math.abs

class MainActivity : AppCompatActivity(), GestureDetector.OnGestureListener {
    lateinit var binding: ActivityMainBinding
    private lateinit var gDetector: GestureDetector
    var stack = LinkedList<String>()
    var backupStack = LinkedList<LinkedList<String>>()
    var newEntry = ""
    var entryMode = false
    var scale = 0
    var positiveSign = true
    lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        this.preferences = PreferenceManager.getDefaultSharedPreferences(this)

        if(preferences.getBoolean("theme", false)){
            binding.myLayout.setBackgroundColor(Color.GRAY)
        }

        try{
            scale = BigDecimal(preferences.getString("scale", "0")).toInt()
        } catch (e: Exception){
            scale = 0
        }

        gDetector = GestureDetector(this, this)
        title = "RPN Calculator"
        setContentView(view)

        binding.zeroButton.setOnClickListener() { newNumber(0) }
        binding.oneButton.setOnClickListener() { newNumber(1) }
        binding.twoButton.setOnClickListener() { newNumber(2) }
        binding.threeButton.setOnClickListener() { newNumber(3) }
        binding.fourButton.setOnClickListener() { newNumber(4) }
        binding.fiveButton.setOnClickListener() { newNumber(5) }
        binding.sixButton.setOnClickListener() { newNumber(6) }
        binding.sevenButton.setOnClickListener() { newNumber(7) }
        binding.eightButton.setOnClickListener() { newNumber(8) }
        binding.nineButton.setOnClickListener() { newNumber(9) }
        binding.dotButton.setOnClickListener() { dot() }
        binding.plusminusButton.setOnClickListener() {newSign()}

        binding.enterButton.setOnClickListener() { enter() }
        binding.acButton.setOnClickListener() { ac() }
        binding.dropButton.setOnClickListener() { drop() }
        binding.swapButton.setOnClickListener() { swap() }
        binding.delButton.setOnClickListener() {del()}
        binding.settingsButton.setOnClickListener() {openSettings()}

        binding.plusButton.setOnClickListener() { operation("ADD") }
        binding.minusButton.setOnClickListener() { operation("SUBTRACT") }
        binding.multiplyButton.setOnClickListener() { operation("MULTIPLY") }
        binding.divideButton.setOnClickListener() { operation("DIVIDE") }
        binding.powerButton.setOnClickListener() { operation("INCREASE") }
        binding.sqrtButton.setOnClickListener() { operation("SQRT") }
    }

    fun openSettings(): Boolean {
        val intent = Intent(this, SettingsActivity::class.java).apply { }
        startActivity(intent)
        return true
    }

    fun newNumber(number: Int) {
        newEntry += number.toString()
        if (!entryMode) {
            entryMode = true
            showEntryMode()
        }
        updateEntry()
    }

    fun newSign(){
        if(positiveSign){
            positiveSign = false
            updateEntry()
            showEntryMode()
        }
        else{
            positiveSign = true
            updateEntry()
            if(newEntry.isEmpty()){
                showNormalMode()
            } else{
                showEntryMode()
            }
        }
    }

    fun swap() {
        if(stack.size > 1){
            backupStack.addFirst(LinkedList(stack))
            val temp1 = stack.removeFirst()
            val temp2 = stack.removeFirst()
            stack.addFirst(temp1)
            stack.addFirst(temp2)

            if (entryMode) {
                showEntryMode()
            } else {
                showNormalMode()
            }
        }
    }

    fun del(){
        if (newEntry.isNotEmpty()){
            newEntry = newEntry.substring(0,newEntry.length-1)
            updateEntry()
        }

        if (newEntry.isEmpty()){
            entryMode = false
            positiveSign = true
            showNormalMode()
        }
    }

    fun dot() {
        if (entryMode and newEntry.isNotEmpty() and !newEntry.contains('.')) {
            newEntry += '.'
            updateEntry()
        }
    }

    fun enter() {
        if (newEntry.isNotEmpty()) {
            backupStack.addFirst(LinkedList(stack))
            if(positiveSign) stack.addFirst(newEntry)
            else stack.addFirst("-$newEntry")
            newEntry = ""
            entryMode = false
            showNormalMode()
            positiveSign = true
        }
    }

    fun ac() {
        backupStack.addFirst(LinkedList(stack))
        newEntry = ""
        stack.clear()
        entryMode = false
        positiveSign = true
        showNormalMode()
    }

    fun drop() {
        backupStack.addFirst(LinkedList(stack))
        stack.removeAt(0)
        if (entryMode) {
            showEntryMode()
        } else {
            showNormalMode()
        }
    }

    fun showEntryMode() {
        val stackSize = stack.size
        entryMode = true
        binding.firstFieldLab.visibility = View.INVISIBLE
        binding.secondFieldLab.text = "1:"
        binding.thirdFieldLab.text = "2:"
        binding.fourthFieldLab.text = "3:"

        binding.secondField.text = ""
        binding.thirdField.text = ""
        binding.fourthField.text = ""

        if (stackSize >= 1) {
            binding.secondField.text = stack.get(0)
        }

        if (stackSize >= 2) {
            binding.thirdField.text = stack.get(1)
        }

        if (stackSize >= 3) {
            binding.fourthField.text = stack.get(2)
        }

        updateEntry()
    }

    @SuppressLint("SetTextI18n")
    fun updateEntry() {
        if(positiveSign) binding.firstField.text = newEntry
        else binding.firstField.text = "-$newEntry"
    }

    fun showNormalMode() {
        val stackSize = stack.size
        entryMode = false
        binding.firstFieldLab.visibility = View.VISIBLE
        binding.firstFieldLab.text = "1:"
        binding.secondFieldLab.text = "2:"
        binding.thirdFieldLab.text = "3:"
        binding.fourthFieldLab.text = "4:"

        binding.firstField.text = ""
        binding.secondField.text = ""
        binding.thirdField.text = ""
        binding.fourthField.text = ""

        if (stackSize >= 1) {
            binding.firstField.text = stack.get(0)
        }

        if (stackSize >= 2) {
            binding.secondField.text = stack.get(1)
        }

        if (stackSize >= 3) {
            binding.thirdField.text = stack.get(2)
        }

        if (stackSize >= 4) {
            binding.fourthField.text = stack.get(3)
        }
    }

    fun operation(operationType: String) {
        backupStack.addFirst(LinkedList(stack))
        if (stack.size >= 2 && setOf("ADD", "SUBTRACT", "MULTIPLY", "DIVIDE", "INCREASE").contains(
                operationType
            )
        ) {
            val firstVal = stack.removeFirst().toBigDecimal()
            val secondVal = stack.removeFirst().toBigDecimal()
            var product = BigDecimal.ZERO

            try {
                when (operationType) {
                    "ADD" -> product = secondVal.add(firstVal)
                    "SUBTRACT" -> product = secondVal.subtract(firstVal)
                    "MULTIPLY" -> product = secondVal.multiply(firstVal)
                    "DIVIDE" -> product = secondVal.divide(firstVal, RoundingMode.HALF_UP)
                    "INCREASE" -> {
                        if (firstVal.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                            product = secondVal.pow(firstVal.toInt())
                        } else {
                            throw Exception("Operation not permitted. First value is not integer")
                        }
                    }
                }

                stack.addFirst(product.setScale(scale, RoundingMode.HALF_UP).toString())
                showNormalMode()

            } catch (e: Exception) {
                Toast.makeText(this, e.localizedMessage.toString(), Toast.LENGTH_LONG).show()
            }

        } else if (stack.size >= 1 && setOf("SQRT").contains(operationType)) {
            val firstVal = stack.removeFirst().toBigDecimal()
            var product = BigDecimal.ZERO

            try {
                product = BigDecimal(sqrt(firstVal.toDouble()))
                stack.addFirst(product.setScale(scale, RoundingMode.HALF_UP).toString())
                showNormalMode()

            } catch (e: Exception) {
                Toast.makeText(this, e.localizedMessage.toString(), Toast.LENGTH_LONG).show()
            }

        }
    }

    fun rollback(){
        if(backupStack.size > 0){
            stack = backupStack.pop()
            if(entryMode) showEntryMode()
            else showNormalMode()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun onDown(p0: MotionEvent?): Boolean {
        return true
    }

    override fun onShowPress(p0: MotionEvent?) {
    }

    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        return true
    }

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        return true
    }

    override fun onLongPress(p0: MotionEvent?) {
    }

    override fun onFling(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        val differenceX = p1.x - p0.x
        val differenceY = p1.y - p0.y
        if(abs(differenceX) > abs(differenceY) && differenceX > 0 && abs(differenceX) > 100 && p2 > 50){

            rollback()
        }
        return true
    }


}