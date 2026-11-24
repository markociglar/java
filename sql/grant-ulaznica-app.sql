USE [Ulaznica];
GO

-- Izvrsiti kao korisnik koji smije dodjeljivati prava u bazi Ulaznica.
GRANT SELECT, INSERT ON OBJECT::[dbo].[korisnici] TO [ulaznica_app];
GRANT SELECT, INSERT, UPDATE ON OBJECT::[dbo].[postavke] TO [ulaznica_app];
GRANT SELECT, INSERT, UPDATE ON OBJECT::[dbo].[dogadjaji] TO [ulaznica_app];
GRANT SELECT, INSERT, DELETE ON OBJECT::[dbo].[narudzbe] TO [ulaznica_app];
GRANT SELECT, INSERT ON OBJECT::[dbo].[racuni] TO [ulaznica_app];
GO
