package com.solutions.inwork.Admin.adapters

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.solutions.inwork.Admin.dataclasses.AdminConnectivity
import com.solutions.inwork.R
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.util.Locale

class ConnectivityAdapter(private val context: Context,var EmpStatusList : List<AdminConnectivity>): RecyclerView.Adapter<ConnectivityAdapter.ConnectivityViewHolder>(){

    fun submitList(newList: List<AdminConnectivity>) {
        EmpStatusList = newList
        notifyDataSetChanged()
    }
  /*  fun isAppInstalled(context: Context, packageName: String?): Boolean {
        return try {
            context.packageManager.getApplicationInfo(packageName!!, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }*/

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ConnectivityViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.connectivity_item,parent,false)
        return ConnectivityViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n", "ResourceAsColor")
    override fun onBindViewHolder(
        holder: ConnectivityViewHolder,
        position: Int,
    ) {
        val Emplist = EmpStatusList[position]
        holder.employee_Id.text = "Employee Id: " + Emplist.employee_id

//        if(isAppInstalled(context,"com.solutions.inwork")){
//            holder.installationstatus.text = " Installation Status : Installed"
//        }
//        else{
//            holder.installationstatus.text = "Installation Status : Uninstalled"
//        }
        val currentTimestamp = System.currentTimeMillis()
        val currentInstant = Instant.ofEpochMilli(currentTimestamp)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val lastOnlineDate = Emplist.updated_time_stamp?.let { dateFormat.parse(it) }
        val lastOnlineTimestamp = lastOnlineDate?.time ?: 0L

        val lastOnlineInstant = Instant.ofEpochMilli(lastOnlineTimestamp)

        val timeDifference = Duration.between(lastOnlineInstant, currentInstant)

        val days = timeDifference.toDays()
        val hours = timeDifference.toHours() % 24
        val minutes = timeDifference.toMinutes() % 60

        val lastOnlineText: String = when {
            days >= 7 -> Emplist.updated_time_stamp.toString()
            days > 0 -> "last Updated $days days ago"
            hours > 0 -> "last Updated $hours hours ago"
            minutes > 0 -> "last Updated $minutes minutes ago"
            else -> "Online"
        }

        holder.onlineStatus.text = lastOnlineText
        if (lastOnlineText == "Online"){
            holder.onlineStatus.setTextColor(ContextCompat.getColor(context, R.color.GREEN))
        }

        holder.viewless.setOnClickListener {
            holder.Connectivitylayout.visibility = View.GONE
            holder.viewmore.visibility = View.VISIBLE
        }
        holder.viewmore.setOnClickListener {
            holder.Connectivitylayout.visibility = View.VISIBLE
            holder.viewmore.visibility = View.GONE
        }
        holder.mapReadyCallback = MapReadyCallback()
        val mapView = holder.ConnectivityMap
        mapView.onCreate(Bundle())
        mapView.getMapAsync (holder.mapReadyCallback!!)

        if (Emplist.location_lat.isNullOrEmpty() || Emplist.location_long.isNullOrEmpty()){
                Log.d("locationConnectivity","null")
        }else{
            markLocationOnMap(mapView, Emplist.location_lat!!.toDouble(), Emplist.location_long!!.toDouble())
            holder.showLocation.setOnClickListener {
                markLocationOnMap(mapView, Emplist.location_lat!!.toDouble(), Emplist.location_long!!.toDouble())
            }
        }

    }

    override fun getItemCount(): Int {
        return EmpStatusList.size
    }

    class ConnectivityViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val employee_Id : TextView = itemView.findViewById(R.id.ConnectivityemployeeIdTextView)
        val onlineStatus : TextView = itemView.findViewById(R.id.LastOnlineTextView)
        val installationstatus : TextView = itemView.findViewById(R.id.installationstatus)
        val ConnectivityMap : MapView = itemView.findViewById(R.id.connectivityMap)
        val viewless : TextView =itemView.findViewById(R.id.viewLessButton)
        val viewmore : TextView = itemView.findViewById(R.id.viewMoreButton)
        val Connectivitylayout : LinearLayout = itemView.findViewById(R.id.ConnectivityLinearLayout)
        val showLocation : TextView = itemView.findViewById(R.id.LocationOnMap)
        var mapReadyCallback: MapReadyCallback? = null


        init {
            ConnectivityMap.onCreate(Bundle())
        }


        fun onViewRecycled() {
            ConnectivityMap.onStop()
        }

        fun onViewAttachedToWindow() {
            ConnectivityMap.onStart()
        }

        fun onViewDetachedFromWindow() {
            ConnectivityMap.onStop()
        }

    }


    private lateinit var googlemap : GoogleMap

    @SuppressLint("SuspiciousIndentation")
    fun markLocationOnMap(mapView: MapView, latitude: Double, longitude: Double) {
        mapView.getMapAsync { googleMap ->
            val location = LatLng(latitude, longitude)
                googleMap.addMarker(MarkerOptions().position(location).title("Current Location"))!!
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))

//            googleMap.setOnMarkerClickListener { clickedMarker ->
//                if (clickedMarker == marker) {
//                    // Retrieve the marker title (tag)
//                    val clickedMarkerTitle = clickedMarker.title
//                    clickedMarker.tag = clickedMarkerTitle
//
//                    // Display the marker title (tag)
//                    Toast.makeText(context, "$clickedMarkerTitle", Toast.LENGTH_SHORT).show()
//                }
//                true
//            }
        }
    }




    inner class MapReadyCallback : OnMapReadyCallback {
        override fun onMapReady(googleMap: GoogleMap) {
            // Perform map setup here
            googlemap = googleMap
            googleMap.uiSettings.isZoomControlsEnabled = true
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            googleMap.isMyLocationEnabled = true
        }
    }

    override fun onViewRecycled(holder: ConnectivityViewHolder) {
        holder.onViewRecycled()
        super.onViewRecycled(holder)
    }

    override fun onViewAttachedToWindow(holder: ConnectivityViewHolder) {
        holder.onViewAttachedToWindow()
        super.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: ConnectivityViewHolder) {
        holder.onViewDetachedFromWindow()
        super.onViewDetachedFromWindow(holder)
    }


}