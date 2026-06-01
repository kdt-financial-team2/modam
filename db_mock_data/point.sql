INSERT INTO point_history (id, couple_id, type, reason, amt, aft_bal, descrip, created_at)
VALUES (NEXT VALUE FOR point_seq, 1, 'SAVE', 'ATTENDANCE', 10000, 10000, '테스트 포인트 지급', CURRENT_TIMESTAMP);
