package com.example.lsm_petani

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.database.*

class ShareFragment : Fragment() {
//
//    private lateinit var listView: ListView
//    private lateinit var firebaseDatabase: FirebaseDatabase
//    private lateinit var dataList: ArrayList<DataFarmers>
//    private lateinit var adapter: ArrayAdapter<DataFarmers>
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_share, container, false)
//
//        listView = view.findViewById(R.id.listView)
//        firebaseDatabase = FirebaseDatabase.getInstance()
//        dataList = ArrayList()
//
//        // Using requireContext() to ensure the context is non-null
//        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, dataList)
//        listView.adapter = adapter
//
//        // Fetch unverified data from Firebase
//        val ref = firebaseDatabase.reference.child("lsm_pertanian")
//        ref.orderByChild("isVerified").equalTo(false).addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                dataList.clear() // Clear the list before adding new data
//                for (dataSnapshot in snapshot.children) {
//                    val data = dataSnapshot.getValue(DataFarmers::class.java)
//                    if (data != null) {
//                        // Add data to the list
//                        dataList.add(data)
//                    }
//                }
//                adapter.notifyDataSetChanged() // Notify adapter to refresh ListView
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(context, "Gagal memuat data", Toast.LENGTH_SHORT).show()
//            }
//        })
//
//        // Set item click listener to confirm data verification
//        listView.setOnItemClickListener { _, _, position, _ ->
//            val selectedData = dataList[position]
//            confirmData(selectedData)
//        }
//
//        return view
//    }
//
//    // Method to confirm data as verified
//    private fun confirmData(data: DataFarmers) {
//        val ref = firebaseDatabase.reference.child("lsm_pertanian").child(data.id)
//        ref.child("isVerified").setValue(true)
//            .addOnSuccessListener {
//                Toast.makeText(requireContext(), "Data berhasil diverifikasi", Toast.LENGTH_SHORT).show()
//                // Memastikan refresh data di HomeFragment setelah verifikasi selesai
//                val homeFragment = parentFragmentManager.findFragmentByTag(HomeFragment::class.java.simpleName) as? HomeFragment
//                homeFragment?.fetchFarmersData() // Memanggil method di HomeFragment untuk fetch data
//            }
//            .addOnFailureListener {
//                Toast.makeText(requireContext(), "Gagal memverifikasi data", Toast.LENGTH_SHORT).show()
//            }
//    }

}
