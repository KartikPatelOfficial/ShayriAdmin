package com.deucate.shayriadmin

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.EditText
import com.google.firebase.firestore.FirebaseFirestore

class TemplateActivity : AppCompatActivity() {

    private val sayries = ArrayList<String>()
    private val sayriesID = ArrayList<String>()
    private val adapter = MainAdapter(sayries)

    private val db = FirebaseFirestore.getInstance()
    private var id = ""

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_template)
        id = intent.getStringExtra("ID")
        val title = "${intent.getStringExtra("Name")} Shayri"
        supportActionBar!!.title = title

        val recyclerView: RecyclerView = findViewById(R.id.templateRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading...")
        progressDialog.setMessage("We are collecting shayries for you..")
        progressDialog.show()

        findViewById<FloatingActionButton>(R.id.templateAddBtn).setOnClickListener {
            showChangeLangDialog()
        }

        adapter.listner = object : MainAdapter.CardClickListener {
            override fun onClickDelete(position: Int) {
                android.app.AlertDialog.Builder(this@TemplateActivity).setTitle("Warning")
                    .setMessage("Are you sure you want to delete?")
                    .setPositiveButton("Ok") { _, _ ->
                        db.collection("Category").document(id).collection("Shayri").document(sayriesID[position])
                            .delete().addOnCompleteListener {
                                if (it.isSuccessful) {
                                    sayries.removeAt(position)
                                    sayriesID.removeAt(position)
                                    adapter.notifyDataSetChanged()
                                } else {
                                    AlertDialog.Builder(this@TemplateActivity).setTitle("Error")
                                        .setMessage(it.exception!!.localizedMessage).create()
                                        .show()
                                }
                            }
                    }.setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }.show()
            }

            override fun onClickCard(position: Int) {}

        }

        db.collection("Category").document(id).collection("Shayri").get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    for (doc in it.result!!) {
                        sayries.add(doc.getString("Text")!!)
                        sayriesID.add(doc.id)
                    }
                    adapter.notifyDataSetChanged()
                    progressDialog.dismiss()
                } else {
                    AlertDialog.Builder(this).setTitle("Error").setMessage(it.exception!!.localizedMessage).create()
                        .show()
                }
            }
    }

    @SuppressLint("InflateParams")
    fun showChangeLangDialog() {
        val dialogBuilder = android.app.AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.custom_dialog, null)
        dialogBuilder.setView(dialogView)

        val edt = dialogView.findViewById(R.id.edit1) as EditText

        dialogBuilder.setTitle("Enter Category Name")
        dialogBuilder.setPositiveButton("Done") { _, _ ->
            val text = edt.text.toString()

            progressDialog.setMessage("updating data...")
            progressDialog.show()

            val data = HashMap<String, Any>()
            data["Text"] = text

            db.collection("Category").document(id).collection("Shayri").add(data).addOnCompleteListener {
                if (it.isSuccessful) {
                    progressDialog.dismiss()
                } else {
                    AlertDialog.Builder(this).setTitle("Error").setMessage(it.exception!!.localizedMessage).create()
                        .show()
                }
            }
        }
        dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        val b = dialogBuilder.create()
        b.show()
    }
}
