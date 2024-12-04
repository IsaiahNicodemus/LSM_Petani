package com.example.lsm_petani

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.example.lsm_petani.model.Farmer

class FarmersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var farmerList: ArrayList<Farmer>
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_farmers, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewFarmers)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        farmerList = ArrayList()
        database = FirebaseDatabase.getInstance().getReference("lsm_pertanian") // Path database

        fetchFarmersData()

        return view
    }

    private fun fetchFarmersData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                farmerList.clear()
                for (data in snapshot.children) {
                    val farmer = data.getValue(Farmer::class.java)
                    if (farmer != null) {
                        farmerList.add(farmer)
                    }
                }
                val adapter = FarmersAdapter(farmerList)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
