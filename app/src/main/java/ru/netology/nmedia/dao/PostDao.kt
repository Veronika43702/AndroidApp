package ru.netology.nmedia.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity ORDER BY case when id <= 0 then -id end DESC, id DESC")
    fun getAll(): LiveData<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Query("SELECT * FROM PostEntity where id <= 0 ORDER BY -id")
    fun getUnsavedPosts(): List<PostEntity>

    @Query("SELECT MIN(id) from PostEntity")
    suspend fun findMinId(): Long

    @Query("DELETE FROM PostEntity WHERE id <= 0")
    suspend fun deleteUnsaved()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("UPDATE PostEntity SET content = :content WHERE id = :id")
    suspend fun updateContentById(id: Long, content: String)

    @Query("""UPDATE PostEntity SET
               likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
               likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
           WHERE id = :id""")
    suspend fun likeById(id: Long)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query( """
           UPDATE PostEntity SET
               share = share + 1
           WHERE id = :id;
        """)
    suspend fun share(id: Long)

}