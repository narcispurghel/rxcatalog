package com.github.narcispurghel.rxcatalog.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.github.narcispurghel.rxcatalog.auth.AuthRepository
import com.github.narcispurghel.rxcatalog.auth.LocalAuthRepository
import com.github.narcispurghel.rxcatalog.auth.PreferencesSessionDataStore
import com.github.narcispurghel.rxcatalog.auth.SessionDataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    @Singleton
    abstract fun bindSessionDataStore(
        sessionDataStore: PreferencesSessionDataStore,
    ): SessionDataStore

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepository: LocalAuthRepository,
    ): AuthRepository

    companion object {
        @Provides
        @Singleton
        fun provideSessionPreferencesDataStore(
            @ApplicationContext context: Context,
        ): DataStore<Preferences> =
            PreferenceDataStoreFactory.create(
                produceFile = { context.preferencesDataStoreFile("session.preferences_pb") },
            )
    }
}
