package com.github.narcispurghel.rxcatalog.di

import com.github.narcispurghel.rxcatalog.catalog.CatalogRepository
import com.github.narcispurghel.rxcatalog.catalog.RoomCatalogRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CatalogModule {
    @Binds
    @Singleton
    abstract fun bindCatalogRepository(
        catalogRepository: RoomCatalogRepository,
    ): CatalogRepository
}

