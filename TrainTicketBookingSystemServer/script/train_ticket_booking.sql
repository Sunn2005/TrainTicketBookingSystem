-- =========================
-- INSERT DATA TRAIN SYSTEM
-- =========================

-- 1. Station
INSERT INTO Station (stationId, name, location) VALUES
('ST01', 'Sai Gon', 'Ho Chi Minh'),
('ST02', 'Nha Trang', 'Khanh Hoa'),
('ST03', 'Da Nang', 'Da Nang'),
('ST04', 'Ha Noi', 'Ha Noi');

-- 2. Train
INSERT INTO Train (trainId, trainName) VALUES
('T01', 'SE1'),
('T02', 'SE2');

-- 3. Schedule
INSERT INTO Schedule (scheduleId, trainId, fromStationId, toStationId, departureTime, arrivalTime) VALUES
('SCH01', 'T01', 'ST01', 'ST04', '2026-04-10 08:00:00', '2026-04-11 06:00:00'),
('SCH02', 'T02', 'ST02', 'ST03', '2026-04-10 09:00:00', '2026-04-10 15:00:00');

-- 4. Seat
INSERT INTO Seat (seatId, trainId, seatNumber, seatType) VALUES
('S01', 'T01', 'A1', 'SOFT'),
('S02', 'T01', 'A2', 'SOFT'),
('S03', 'T01', 'B1', 'HARD'),
('S04', 'T02', 'A1', 'SOFT'),
('S05', 'T02', 'A2', 'SOFT');

-- 5. Customer
INSERT INTO Customer (customerId, name, phone, email) VALUES
('C01', 'Nguyen Van A', '0901234567', 'a@gmail.com'),
('C02', 'Tran Thi B', '0912345678', 'b@gmail.com');

-- 6. Ticket
INSERT INTO Ticket (ticketId, scheduleId, seatId, customerId, status, price) VALUES
('TK01', 'SCH01', 'S01', 'C01', 'PAID', 500000),
('TK02', 'SCH01', 'S02', 'C02', 'PAID', 500000);

-- 7. Payment
INSERT INTO Payment (paymentId, ticketId, amount, method, status) VALUES
('P01', 'TK01', 500000, 'CASH', 'SUCCESS'),
('P02', 'TK02', 500000, 'QR', 'SUCCESS');

-- 8. Employee
INSERT INTO Employee (roleId, name, role, status) VALUES
('E01', 'Le Van Staff', 'STAFF', 'ACTIVE'),
('E02', 'Tran Van Manager', 'MANAGER', 'ACTIVE');

-- 9. Price
INSERT INTO Price (priceId, fromStationId, toStationId, seatType, price) VALUES
('PR01', 'ST01', 'ST04', 'SOFT', 500000),
('PR02', 'ST01', 'ST04', 'HARD', 300000),
('PR03', 'ST02', 'ST03', 'SOFT', 200000);