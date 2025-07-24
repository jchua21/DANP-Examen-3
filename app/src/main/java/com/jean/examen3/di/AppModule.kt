package com.jean.examen3.di

import android.content.Context
import com.jean.examen3.data.local.UserDataStore
import com.jean.examen3.data.repository.DetectedContactRepository
import com.jean.examen3.data.repository.UserRepository
import com.jean.examen3.presentation.BleScanner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideUserDataStore(@ApplicationContext context: Context): UserDataStore {
        return UserDataStore(context)
    }

    @Provides
    @Singleton
    fun provideUserRepository(userDataStore: UserDataStore): UserRepository {
        return UserRepository(userDataStore)
    }

    @Provides
    @Singleton
    fun provideDetectedContactRepository(): DetectedContactRepository {
        return DetectedContactRepository()
    }

    @Provides @Singleton
    fun provideBleScanner(@ApplicationContext ctx: Context): BleScanner =
        BleScanner(ctx)
}
