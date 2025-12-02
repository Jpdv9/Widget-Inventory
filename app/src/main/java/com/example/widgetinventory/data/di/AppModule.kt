package com.example.widgetinventory.di

import com.example.widgetinventory.data.repository.ProductRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Aquí le enseñamos a Hilt que cuando alguien pida FirebaseAuth,
    // debe ejecutar FirebaseAuth.getInstance()
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    // Probablemente también necesites esto si ProductRepository no tiene @Inject en su constructor
    @Provides
    @Singleton
    fun provideProductRepository(): ProductRepository {
        return ProductRepository()
    }
}