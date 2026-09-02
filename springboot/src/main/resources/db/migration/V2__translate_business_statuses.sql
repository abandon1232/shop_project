UPDATE business SET status = 'PENDING' WHERE status = '审核中';
UPDATE business SET status = 'APPROVED' WHERE status = '审核通过';
UPDATE business SET status = 'REJECTED' WHERE status = '审核不通过';

ALTER TABLE business
    MODIFY COLUMN status VARCHAR(32) NOT NULL DEFAULT 'PENDING';
