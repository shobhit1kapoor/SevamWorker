package com.sevam.customer.di

import com.sevam.customer.partner.data.InMemoryPartnerAuthTokenProvider
import com.sevam.customer.partner.data.PartnerAuthTokenProvider
import com.sevam.customer.partner.data.PartnerRepository
import com.sevam.customer.partner.data.RemoteFirstPartnerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PartnerDataModule {
    @Binds
    @Singleton
    abstract fun bindPartnerRepository(repository: RemoteFirstPartnerRepository): PartnerRepository

    @Binds
    @Singleton
    abstract fun bindPartnerAuthTokenProvider(provider: InMemoryPartnerAuthTokenProvider): PartnerAuthTokenProvider
}
