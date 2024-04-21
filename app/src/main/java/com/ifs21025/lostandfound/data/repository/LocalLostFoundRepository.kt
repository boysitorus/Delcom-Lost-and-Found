package com.ifs21025.lostandfound.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.ifs21025.lostandfound.data.local.entity.DelcomLostFoundEntity
import com.ifs21025.lostandfound.data.local.room.DelcomLostFoundDatabase
import com.ifs21025.lostandfound.data.local.room.IDelcomLostFoundDao
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LocalLostFoundRepository(context: Context) {
    private val mDelcomLostFoundDao: IDelcomLostFoundDao
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    init {
        val db = DelcomLostFoundDatabase.getInstance(context)
        mDelcomLostFoundDao = db.delcomLostFoundDao()
    }
    fun getAllLostFounds(): LiveData<List<DelcomLostFoundEntity>?> = mDelcomLostFoundDao.getAllLostFounds()

    fun get(lostFoundId: Int): LiveData<DelcomLostFoundEntity?> = mDelcomLostFoundDao.get(lostFoundId)

    fun insert(lostFound: DelcomLostFoundEntity) {
        executorService.execute { mDelcomLostFoundDao.insert(lostFound) }
    }

    fun delete(lostFound: DelcomLostFoundEntity) {
        executorService.execute { mDelcomLostFoundDao.delete(lostFound) }
    }

    companion object {
        @Volatile
        private var INSTANCE: LocalLostFoundRepository? = null
        fun getInstance(
            context: Context
        ): LocalLostFoundRepository {
            synchronized(LocalLostFoundRepository::class.java) {
                INSTANCE = LocalLostFoundRepository(
                    context
                )
            }
            return INSTANCE as LocalLostFoundRepository
        }
    }

}