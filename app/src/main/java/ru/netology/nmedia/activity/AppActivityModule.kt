package ru.netology.nmedia.activity

import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppActivityModule {
    @Singleton
    @Provides
    fun providesGoogleApiAvailability() = GoogleApiAvailability.getInstance()

    @Singleton
    @Provides
    fun providesFirebaseMessaging() = FirebaseMessaging.getInstance()
}