package com.solutions.inwork.Admin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.solutions.inwork.Admin.fragments.*
import com.solutions.inwork.GpsNotificationService
import com.solutions.inwork.LogInActivity
import com.solutions.inwork.R
import com.solutions.inwork.client.adapter.PermissionAdapter
import com.solutions.inwork.client.dataclasses.PermissionItem
import com.solutions.inwork.client.fragments.ContactUsFragment
import com.solutions.inwork.databinding.ActivityAdminBinding
import java.lang.Exception
import java.lang.NullPointerException

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private val PERMISSION_REQUEST_CODE = 100
    private val permissionsList = listOf(
        PermissionItem("Location", "For usage of maps."),
        PermissionItem("Storage Permission", "For the download of Pdfs."),
        PermissionItem("Notifications", "To Post Notifications")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpDrawerLayout()

        requestStoragePermission()

        loadFragment(AdminHomeFragment())

        stopService(Intent(this, GpsNotificationService::class.java))

        intent.getStringExtra("fragment")?.let { fragmentName ->
            Log.d("fg", fragmentName)
            if (fragmentName == "Notification") {
                binding.appbarText.text = "Notifications"
                loadFragment(AdminGetNotifcationFragment())
            }
        }

        binding.profileadmin.setOnClickListener {
            binding.appbarText.text = "Profile"
            loadFragment(AdminProfileFragment())
        }

        binding.menubtn.setOnClickListener {
            if (!binding.drawerLayout.isDrawerOpen(binding.NavigationView)) {
                binding.drawerLayout.openDrawer(binding.NavigationView)
            }
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val currentFragment = getCurrentFragment()
            currentFragment?.let {
                updateAppBarText(it)
            }
        }


        binding.bottomNavigation.background = null
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.bottom_home ->loadFragment(AdminHomeFragment())
                R.id.bottom_notificationadmin ->loadFragment(AdminGetNotifcationFragment())
                R.id.bottom_addemployee ->loadFragment(AddEmployeeFragment())
                R.id.bottom_sentnotice -> loadFragment(AdmingetNotice())

            }
            true

        }

        binding.fab.setOnClickListener{
            loadFragment(SendNoticeFragment())

        }
    }

    override fun onBackPressed() {
            if (binding.drawerLayout.isDrawerOpen (GravityCompat.START)) {
                binding.drawerLayout.closeDrawer (GravityCompat.START)
            } else {
                val currentFragment = getCurrentFragment()
                if (currentFragment is AdminHomeFragment) {
                    finish()
                } else {
                    super.onBackPressed()
                }
            }
    }

    private fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@AdminActivity, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.admincontainer, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun setUpDrawerLayout() {
        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, 0, R.string.app_name)
        toggle.syncState()

        val headerView = binding.NavigationView.getHeaderView(0)
        headerView.findViewById<TextView>(R.id.UserName).text = getCompanyName()
        headerView.findViewById<TextView>(R.id.Emailtxt).text = FirebaseAuth.getInstance().currentUser?.email

        binding.NavigationView.setNavigationItemSelectedListener { menuItem ->
            handleNavigationItemSelection(menuItem.itemId)
            true
        }
    }

    private fun handleNavigationItemSelection(itemId: Int) {
        when (itemId) {
            R.id.homebtn -> loadFragment(AdminHomeFragment()).also {
                binding.appbarText.text = "Home"
            }
            R.id.profilebtn -> loadFragment(AdminProfileFragment()).also {
                binding.appbarText.text = "Profile"
            }
            R.id.sentnotices -> loadFragment(AdmingetNotice()).also {
                binding.appbarText.text = "Sent Notices"
            }
            R.id.addevent -> loadFragment(AddEvent()).also {
                binding.appbarText.text = "Add Event"
            }
            R.id.empbtn -> loadFragment(AllEmployeesFragment()).also {
                binding.appbarText.text = "Employees"
            }
            R.id.btnleavereq -> loadFragment(LeaveReuestFragment()).also {
                binding.appbarText.text = "Leave Request"
            }
            R.id.Noticebtn -> loadFragment(SendNoticeFragment()).also {
                binding.appbarText.text = "Notice"
            }
            R.id.btnAddEmployee -> loadFragment(AddEmployeeFragment()).also {
                binding.appbarText.text = "Add Employee"
            }
            R.id.btnScreenDetails -> loadFragment(AdminScreenDetailsFragment()).also {
                binding.appbarText.text = "Screen Details"
            }
            R.id.SettingsBtn -> PermissionDialog()
            R.id.EmpMonthlyReports -> loadFragment(EmployeeReportFragment()).also {
                binding.appbarText.text = "Monthly Reports"
            }
            R.id.btnStatusEmployee -> loadFragment(EmployeeStatusFragment()).also {
                binding.appbarText.text = "Employee Status"
            }
            R.id.AdmincontactUsbtn -> loadFragment(ContactUsFragment()).also {
                binding.appbarText.text = "Contact Us"
            }
            R.id.logoutbtn -> logoutUser()
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun getCurrentFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(R.id.admincontainer)
    }

    private fun updateAppBarText(fragment: Fragment) {
        binding.appbarText.text = when (fragment) {
            is AdminHomeFragment -> "Home"
            is AdminGetNotifcationFragment -> "Notification"
            is AdminProfileFragment -> "Profile"
            is AdmingetNotice -> "Sent Notices"
            is AddEvent -> "Add Event"
            is AllEmployeesFragment -> "Employees"
            is LeaveReuestFragment -> "Leave Request"
            is SendNoticeFragment -> "Notice"
            is AddEmployeeFragment -> "Add Employee"
            is AdminScreenDetailsFragment -> "Screen Details"
            is EmployeeReportFragment -> "Monthly Reports"
            is EmployeeStatusFragment -> "Employee Status"
            is ContactUsFragment -> "Contact Us"
            else -> "Unknown Fragment"
        }
    }

    private fun getCompanyName(): String {
        val sharedPreferences1 = getSharedPreferences("ADMIN_PREF", MODE_PRIVATE)
        return sharedPreferences1.getString("company_name", "Username").toString()
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        val sharedPreferences = getSharedPreferences("ADMIN_PREF", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("company_id", null)
        editor.putString("fcm", null)
        editor.putString("company_name", null)
        editor.apply()
        startActivity(Intent(this, LogInActivity::class.java))
        finish()
    }

    private fun PermissionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_permission_layout, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.dialogRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = PermissionAdapter(permissionsList, dialog)
        dialog.show()
    }
}
