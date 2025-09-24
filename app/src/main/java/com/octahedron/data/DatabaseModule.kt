package com.octahedron.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "my_database.db")
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides fun provideArtistDao(db: AppDatabase) = db.artistDao()
    @Provides fun provideTrackDao(db: AppDatabase) = db.trackDao()
    @Provides fun provideTrackArtistDao(db: AppDatabase) = db.trackArtistDao()
    @Provides fun provideListeningHistoryDao(db: AppDatabase) = db.listeningHistoryDao()
    @Provides fun provideAlbumDao(db: AppDatabase) = db.albumDao()
    @Provides fun provideTrackAlbumDao(db: AppDatabase) = db.trackAlbumDao()
}