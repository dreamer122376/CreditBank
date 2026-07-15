package com.creditbank.mvp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creditbank.mvp.dto.NotificationItemDTO;
import com.creditbank.mvp.entity.NotificationRecipient;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface NotificationRecipientMapper extends BaseMapper<NotificationRecipient> {

    @Select({"<script>",
            "SELECT n.id AS id, n.event_code AS eventCode, n.category AS category,",
            "n.level AS level, n.title AS title, n.content AS content,",
            "n.action_path AS actionPath, n.created_at AS createdAt, r.read_at AS readAt",
            "FROM notification_recipient r JOIN notification n ON n.id = r.notification_id",
            "WHERE r.user_id = #{userId} AND n.status = 'PUBLISHED'",
            "AND (n.expires_at IS NULL OR n.expires_at &gt; NOW())",
            "<if test=\"readStatus == 'UNREAD'\">AND r.read_at IS NULL</if>",
            "<if test=\"readStatus == 'READ'\">AND r.read_at IS NOT NULL</if>",
            "<if test=\"category != null and category != ''\">AND n.category = #{category}</if>",
            "ORDER BY n.id DESC LIMIT #{offset}, #{size}",
            "</script>"})
    List<NotificationItemDTO> selectPageForUser(@Param("userId") Long userId,
                                                @Param("readStatus") String readStatus,
                                                @Param("category") String category,
                                                @Param("offset") int offset,
                                                @Param("size") int size);

    @Select({"<script>",
            "SELECT COUNT(*) FROM notification_recipient r",
            "JOIN notification n ON n.id = r.notification_id",
            "WHERE r.user_id = #{userId} AND n.status = 'PUBLISHED'",
            "AND (n.expires_at IS NULL OR n.expires_at &gt; NOW())",
            "<if test=\"readStatus == 'UNREAD'\">AND r.read_at IS NULL</if>",
            "<if test=\"readStatus == 'READ'\">AND r.read_at IS NOT NULL</if>",
            "<if test=\"category != null and category != ''\">AND n.category = #{category}</if>",
            "</script>"})
    long countForUser(@Param("userId") Long userId,
                      @Param("readStatus") String readStatus,
                      @Param("category") String category);

    @Select("SELECT COUNT(*) FROM notification_recipient r " +
            "JOIN notification n ON n.id = r.notification_id " +
            "WHERE r.user_id = #{userId} AND r.read_at IS NULL AND n.status = 'PUBLISHED' " +
            "AND (n.expires_at IS NULL OR n.expires_at > NOW())")
    long countUnread(@Param("userId") Long userId);

    @Update("UPDATE notification_recipient SET read_at = COALESCE(read_at, NOW()) " +
            "WHERE notification_id = #{notificationId} AND user_id = #{userId}")
    int markRead(@Param("notificationId") Long notificationId, @Param("userId") Long userId);

    @Update("UPDATE notification_recipient r JOIN notification n ON n.id = r.notification_id " +
            "SET r.read_at = NOW() WHERE r.user_id = #{userId} AND r.read_at IS NULL " +
            "AND n.status = 'PUBLISHED'")
    int markAllRead(@Param("userId") Long userId);
}
