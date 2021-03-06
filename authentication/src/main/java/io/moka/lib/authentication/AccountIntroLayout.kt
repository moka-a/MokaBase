package io.moka.lib.authentication

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.moka.lib.authentication.util.AuthContract
import io.moka.lib.base.util.attr
import io.moka.lib.base.util.onClick
import io.moka.lib.base.util.spannableText
import io.moka.lib.base.util.visible
import kotlinx.android.synthetic.main.layout_account_intro.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class AccountIntroLayout : AccountAuthenticatorActivity() {

    private val accountManager by lazy { AccountManager.get(this) }
    private val adapter by lazy { AccountAdapter(this) }
    private var noVisible = false

    /**
     * LifeCycle functions
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_account_intro)

        initView()
        bindView()
        initData()
    }

    override fun onBackPressed() {
        (supportFragmentManager.findFragmentByTag(AccountLayout::class.java.simpleName) as? AccountLayout)?.onBackPressed()

        if (noVisible) {
            finish()
        }
        else {
            super.onBackPressed()
        }
    }

    /**
     */

    private fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, true)
        recyclerView.adapter = adapter
    }

    private fun bindView() {
        textView_signIn.onClick { onClickToSignIn() }
        textView_anotherAccount.onClick { onClickAnotherSignIn() }
        textView_signUp.onClick { onClickSignUp() }

        adapter.onSelectedListener = { data -> onSelected(data) }
    }

    private fun initData() {
        val tokenLabel = intent.getStringExtra(AccountManager.KEY_AUTH_TOKEN_LABEL)
        if (tokenLabel.isNullOrEmpty()) {
            view_container_wall.visible()
            noVisible = true

            onClickAnotherSignIn(true)
            return
        }

        /* */
        val accounts = accountManager.getAccountsByType(AuthContract.ACCOUNT_TYPE)

        if (accounts.isEmpty()) {
            view_container_wall.visible()
            noVisible = true

            onClickAnotherSignIn(true)
        }
        else {
            noVisible = false

            accounts.forEach { account ->
                val email = accountManager.getUserData(account, "email")
                val profileImage = accountManager.getUserData(account, "profile_image")
                val nickname = account.name

                val data = AccountAdapter.Data(profileImage, nickname, email)
                adapter.add(data)
            }

            /* */
            adapter.selectedData = adapter.items[0]
            onSelected(adapter.items[0])
            adapter.notifyDataSetChanged()
        }
    }

    private fun onSelected(data: AccountAdapter.Data) {
        textView_signIn.text = spannableText(
                attr("⌜ ", isBold = true, ratio = 1f),
                attr("${data.nickname}", isBold = true),
                attr(" ⌟", isBold = true, ratio = 1f),
                attr(" 으로 로그인 하기"))
    }

    private fun getTokensAndFinish(account: Account) = CoroutineScope(Dispatchers.Main).launch {
        val deferred = async(Dispatchers.IO) {
            accountManager.getAuthToken(account, AuthContract.ACCOUNT_TYPE, null, null, null, null)
        }

        val future = deferred.await()
        async(Dispatchers.IO) {
            val bundle = future.result

            val authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN)
            val email = accountManager.getUserData(account, "email")
            val nickname = account.name

            /* */
            val intent = Intent()
            val data = Bundle()
            data.putString("token", authToken)
            data.putString("email", email)
            data.putString("name", nickname)
            intent.putExtras(data)

            setAccountAuthenticatorResult(data)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }.await()

        if (!isFinishing)
            dismissLoadingDialog()
    }

    private fun onClickToSignIn() {
        showLoadingDialog()

        val accounts = accountManager.getAccountsByType(AuthContract.ACCOUNT_TYPE)
        val selectedData = adapter.selectedData

        val account = accounts.filter { accountManager.getUserData(it, "email") == selectedData?.email }[0]
        getTokensAndFinish(account)
    }

    /**
     */

    private fun onClickAnotherSignIn(noSlide: Boolean? = false) {
        supportFragmentManager
                .beginTransaction()
                .apply {
                    if (noSlide == true)
                        setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                    else
                        setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right)
                }
                .add(R.id.view_container, AccountLayout().apply { startFrom = AccountLayout.STATE_SIGN_IN }, AccountLayout::class.java.simpleName)
                .addToBackStack(AccountLayout.toString())
                .commitAllowingStateLoss()
    }

    private fun onClickSignUp() {
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right)
                .add(R.id.view_container, AccountLayout().apply { startFrom = AccountLayout.STATE_SIGN_UP }, AccountLayout::class.java.simpleName)
                .addToBackStack(AccountLayout.toString())
                .commitAllowingStateLoss()
    }

    private val loadingDialog by lazy {
        AlertDialog
                .Builder(this)
                .setView(layoutInflater.inflate(R.layout.dialog_loading, null))
                .setCancelable(false)
                .create()
    }

    fun showLoadingDialog() {
        if (!loadingDialog.isShowing)
            loadingDialog!!.show()
    }

    fun dismissLoadingDialog() {
        if (null != loadingDialog && loadingDialog!!.isShowing)
            loadingDialog!!.dismiss()
    }

}
