ALTER TABLE books
ADD COLUMN rating DECIMAL(3,2) CHECK (rating >= 0 AND rating <= 5);
