package com.jacks.moviet.testutil

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

/** Integration tests own their dependencies and never start advertising or the production DI graph. */
class IsolatedTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader, className: String, context: Context): Application =
        super.newApplication(cl, Application::class.java.name, context)
}
