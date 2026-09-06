package com.wifite.security.listener
interface AttackListener { fun onStart() fun onProgress(p: Int) fun onComplete(pwd: String) }
