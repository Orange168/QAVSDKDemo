package com.tencent.avsdk.activity;

import java.util.ArrayList;

import com.tencent.avsdk.R;
import com.tencent.avsdk.util.Util;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class AddNewQQActivity extends Activity implements OnClickListener,
		TextWatcher {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_new_qq_activity);
		findViewById(R.id.ok).setOnClickListener(this);
		findViewById(R.id.cancel).setOnClickListener(this);
		EditText editTextQQ = (EditText) findViewById(R.id.edit_text_qq);
		EditText editTextIdentifier = (EditText) findViewById(R.id.edit_text_identifier);
		EditText editTextUsersig = (EditText) findViewById(R.id.edit_text_usersig);
		editTextQQ.addTextChangedListener(this);
		editTextIdentifier.addTextChangedListener(this);
		editTextUsersig.addTextChangedListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ok:
			save();
			setResult(RESULT_OK);
			finish();
			break;
		case R.id.cancel:
			finish();
			break;
		default:
			break;
		}
	}

	private void save() {
		ArrayList<String> qqList = Util.getQQList(this);
		ArrayList<String> identifierList = Util.getIdentifierList(this);
		ArrayList<String> usersigList = Util.getUserSigList(this);
		EditText editTextQQ = (EditText) findViewById(R.id.edit_text_qq);
		EditText editTextIdentifier = (EditText) findViewById(R.id.edit_text_identifier);
		EditText editTextUserSig = (EditText) findViewById(R.id.edit_text_usersig);
		qqList.add(editTextQQ.getText().toString());
		identifierList.add(editTextIdentifier.getText().toString());
		usersigList.add(editTextUserSig.getText().toString());
		Util.setQQList(this, qqList);
		Util.setIdentifierList(this, identifierList);
		Util.setUserSigList(this, usersigList);
	}

	@Override
	public void afterTextChanged(Editable s) {
		EditText editTextQQ = (EditText) findViewById(R.id.edit_text_qq);
		EditText editTextIdentifier = (EditText) findViewById(R.id.edit_text_identifier);
		EditText editTextUsersig = (EditText) findViewById(R.id.edit_text_usersig);
		String stringQQ = editTextQQ.getText().toString();
		String stringIdentifier = editTextIdentifier.getText().toString();
		String stringUsersig = editTextUsersig.getText().toString();
		findViewById(R.id.ok).setEnabled(
				stringQQ != null && stringQQ.length() > 0
						&& stringIdentifier != null && stringIdentifier.length() > 0
						&& stringUsersig != null && stringUsersig.length() > 0);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub

	}
}