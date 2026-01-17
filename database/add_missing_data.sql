-- Add missing 'Eclectic' style
INSERT INTO SPECIALTIES (SpecialtyName, Description) VALUES 
('Eclectic', 'Mix of styles, bold, creative')
ON CONFLICT (SpecialtyName) DO NOTHING;

INSERT INTO CATEGORIES (CategoryName, Description) VALUES 
('Eclectic', 'Mix of styles, bold, creative')
ON CONFLICT (CategoryName) DO NOTHING;
