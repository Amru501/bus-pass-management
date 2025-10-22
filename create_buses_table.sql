-- ============================================
-- Create Buses Table and Sample Data
-- ============================================

USE buspassdb;

-- Create buses table if it doesn't exist
CREATE TABLE IF NOT EXISTS buses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bus_number VARCHAR(255) NOT NULL UNIQUE,
    route VARCHAR(255) NOT NULL,
    seats INT NOT NULL,
    schedule VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert sample bus data
INSERT INTO buses (bus_number, route, seats, schedule) VALUES
('MH 01 AB 1234', 'Campus to Downtown', 50, 'Daily 8:00 AM'),
('MH 02 CD 5678', 'Downtown to Campus', 45, 'Daily 6:30 AM'),
('MH 03 EF 9012', 'Airport to Campus', 40, 'Daily 7:15 AM'),
('MH 04 GH 3456', 'Campus to Mall', 35, 'Daily 9:00 AM'),
('MH 05 IJ 7890', 'Mall to Campus', 30, 'Daily 5:30 PM')
ON DUPLICATE KEY UPDATE
    route = VALUES(route),
    seats = VALUES(seats),
    schedule = VALUES(schedule);

-- Verify the data
SELECT * FROM buses;
