USE gym_db;

-- Use this only if your old members.id column is not AUTO_INCREMENT.
-- These foreign keys temporarily block changing members.id, so we remove them,
-- update the column, and add them back.

ALTER TABLE payments DROP FOREIGN KEY payments_ibfk_1;
ALTER TABLE attendance DROP FOREIGN KEY attendance_ibfk_1;
ALTER TABLE workout_schedule DROP FOREIGN KEY workout_schedule_ibfk_1;

ALTER TABLE members MODIFY id INT AUTO_INCREMENT;

ALTER TABLE payments
ADD CONSTRAINT payments_ibfk_1
FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE;

ALTER TABLE attendance
ADD CONSTRAINT attendance_ibfk_1
FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE;

ALTER TABLE workout_schedule
ADD CONSTRAINT workout_schedule_ibfk_1
FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE;

