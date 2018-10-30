package com.deucate.shayriadmin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.EditText


class MainActivity : AppCompatActivity() {


    private val ids = ArrayList<String>()
    private val titles = ArrayList<String>()
    private val adapter = MainAdapter(titles)
    private lateinit var progressDialog: ProgressDialog
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar!!.title = "Categories"

        findViewById<FloatingActionButton>(R.id.mainFABBtn).setOnClickListener {
            showChangeLangDialog()
        }

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading...")
        progressDialog.setMessage("We are collecting shayries for you..")
        progressDialog.show()

        val recyclerView: RecyclerView = findViewById(R.id.mainRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        adapter.listner = object : MainAdapter.CardClickListener {
            override fun onClickCard(position: Int) {
                val intent = Intent(this@MainActivity, TemplateActivity::class.java)
                intent.putExtra("ID", ids[position])
                intent.putExtra("Name", titles[position])
                startActivity(intent)
            }

            override fun onClickDelete(position: Int) {
                AlertDialog.Builder(this@MainActivity).setTitle("Warning")
                    .setMessage("You will loose all shayri for ${titles[position]} and cannot recover")
                    .setPositiveButton("Ok") { _, _ ->
                        db.collection("Category").document(ids[position]).delete().addOnCompleteListener {
                            if (it.isSuccessful) {
                                titles.removeAt(position)
                                ids.removeAt(position)
                                adapter.notifyDataSetChanged()
                            } else {
                                android.support.v7.app.AlertDialog.Builder(this@MainActivity).setTitle("Error")
                                    .setMessage(it.exception!!.localizedMessage).create()
                                    .show()
                            }
                        }
                    }.setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }.show()
            }
        }

        db.collection("Category").get().addOnCompleteListener {
            progressDialog.dismiss()
            if (it.isSuccessful) {
                for (doc in it.result!!) {
                    ids.add(doc.id)
                    titles.add(doc.getString("Name")!!)
                }
                adapter.notifyDataSetChanged()
                progressDialog.dismiss()
            } else {
                AlertDialog.Builder(this).setTitle("Error").setMessage(it.exception!!.localizedMessage).create().show()
            }
        }

    }

    @SuppressLint("InflateParams")
    fun showChangeLangDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.custom_dialog, null)
        dialogBuilder.setView(dialogView)

        val edt = dialogView.findViewById(R.id.edit1) as EditText

        dialogBuilder.setTitle("Enter Category Name")
        dialogBuilder.setPositiveButton("Done") { _, _ ->
            val name = edt.text.toString()

            progressDialog.setMessage("updating data...")
            progressDialog.show()

            val data = HashMap<String, Any>()
            data["Name"] = name

            db.collection("Category").add(data).addOnCompleteListener {
                if (it.isSuccessful) {
                    progressDialog.dismiss()
                    adapter.notifyDataSetChanged()
                } else {
                    android.support.v7.app.AlertDialog.Builder(this).setTitle("Error")
                        .setMessage(it.exception!!.localizedMessage).create()
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
