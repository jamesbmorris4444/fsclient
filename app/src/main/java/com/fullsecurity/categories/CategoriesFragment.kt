package com.fullsecurity.categories

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fullsecurity.demoapplication.ClientCommunicationsWrapper
import com.fullsecurity.demoapplication.SimpleDividerItemDecoration
import com.fullsecurity.server.StoreDBCreator
import com.fullsecurity.shared.MainActivity
import com.fullsecurity.shared.R
import com.fullsecurity.storeitem.StoreItem
import java.util.*

class CategoriesFragment(private val ctxt: Context, var key: ByteArray, private val mainActivity: MainActivity, var userId: Int) : ClientCommunicationsWrapper(key) {
    private val storeItems: ArrayList<StoreItem>
    private val requestedMSName: String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val recyclerView = mainActivity.findViewById<View>(R.id.categoriesRecyclerView) as RecyclerView
        val layoutManager = LinearLayoutManager(ctxt)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
        val toolbar = mainActivity.findViewById<View>(R.id.toolbar) as Toolbar
        mainActivity.setSupportActionBar(toolbar)
        toolbar.title = "CATEGORIES"
        initialCategoriesScreen()
        return inflater.inflate(R.layout.categories_list_main, container, false)
    }

    fun initialCategoriesScreen() {
        val storeDBCreator = mainActivity.storeDBCreator
        val category = StoreDBCreator.CATEGORY
        val description = StoreDBCreator.DESCRIPTION
        val cost = StoreDBCreator.COST
        val table = StoreDBCreator.TABLE_PRODUCTS
        val selectQuery = "SELECT DISTINCT $category FROM $table;|"
        getDataFromMicroservice(selectQuery, "CategoriesFragment", userId, "categories")
    }

    override fun processNormalResponseFromMicroservice(result: String) {
        val s = result.split("[|]").toTypedArray()
        val sb = StringBuffer()
        val n = s.size
        for (i in 0 until n) storeItems.add(StoreItem(s[i], "", 0, 0))
        mainActivity.initalizeDecorations(storeItems) // storeItems contains only unique categories
        loadCategoriesRecyclerView()
    }

    override fun processErrorResponseFromMicroservice(errorMessage: String) {
        var errorMicroserviceName: String? = null
        val errorValueOrNormalReturnValue = -errorMessage.substring(4, 6).toInt()
        if (errorMessage.length > 6) errorMicroserviceName = errorMessage.substring(7)
        storeItems.add(StoreItem(messageGetter(errorValueOrNormalReturnValue, errorMicroserviceName), "ERROR IN REQUEST", -1, -1))
        mainActivity.initalizeDecorations(storeItems)
        loadCategoriesRecyclerView()
    }

    private fun loadCategoriesRecyclerView() {
        if (storeItems.size == 0) storeItems.add(StoreItem("NO", "DATA", -1, -1))
        val recyclerView = activity!!.findViewById<View>(R.id.categoriesRecyclerView) as RecyclerView
        recyclerView.addItemDecoration(SimpleDividerItemDecoration(ctxt))
        val adapter: RecyclerView.Adapter<*> = CategoriesRecyclerViewAdapter(storeItems)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter
    }

    init {
        storeItems = ArrayList()
    }
}