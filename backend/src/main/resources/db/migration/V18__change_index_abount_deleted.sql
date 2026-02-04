CREATE INDEX idx_pickeat_deleted_at_updated_at
ON pickeat (deleted_at, updated_at);

CREATE INDEX idx_room_user_room_id_deleted_at
ON room_user (room_id, deleted_at);

CREATE INDEX idx_room_user_user_id_deleted_at
ON room_user (user_id, deleted_at);

DROP INDEX idx_pickeat_deleted_updated_at ON pickeat;
DROP INDEX idx_room_user_room_id_deleted ON room_user;
DROP INDEX idx_room_user_user_id_deleted ON room_user;
