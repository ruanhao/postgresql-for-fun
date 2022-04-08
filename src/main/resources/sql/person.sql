DROP TABLE IF EXISTS person;
create table person (
    id serial PRIMARY KEY,
	name VARCHAR(50) NOT NULL,
	first_name VARCHAR(10),
	age int,
	gender VARCHAR(10),
	country VARCHAR(30),
	company VARCHAR(50)
);
insert into person (name, first_name, age, gender, country, company) values ('Madonna', 'Peter', 45, 'Male', 'China', 'Fritsch-Walsh');
insert into person (name, first_name, age, gender, country, company) values ('Elvis', 'John', 45, 'Female', 'China', 'Beatty-Howell');
insert into person (name, first_name, age, gender, country, company) values ('Clareta', 'Mary', 36, 'Male', 'China', 'Grant-Nader');
insert into person (name, first_name, age, gender, country, company) values ('Cobby', 'Jack', 21, 'Female', 'China', 'Bernhard Inc');
insert into person (name, first_name, age, gender, country, company) values ('Ardelis', 'Amy', 46, 'Female', 'China', 'Leuschke and Stroman');
insert into person (name, first_name, age, gender, country, company) values ('Obed', 'Doe', 47, 'Male', 'China', 'Kuhn Inc');
insert into person (name, first_name, age, gender, country, company) values ('Lenore', 'Joe', 43, 'Female', 'Russia', 'Ledner and Sons');
insert into person (name, first_name, age, gender, country, company) values ('Federico', 'May', 38, 'Female', 'China', 'Cormier and Greenholt');
insert into person (name, first_name, age, gender, country, company) values ('Gerty', 'Maven', 37, 'Male', 'United States', 'Leuschke-Lynch');
insert into person (name, first_name, age, gender, country, company) values ('Arvin', 'Pony', 36, 'Female', 'China', 'Armstrong Inc');
insert into person (name, first_name, age, gender, country, company) values ('Nisse', 'Paul', 20, 'Female', 'China', 'Schinner and Beatty');
insert into person (name, first_name, age, gender, country, company) values ('Nate', 'Jimmy', 37, 'Male', 'United States', 'Hansen-Weber');
insert into person (name, first_name, age, gender, country, company) values ('Hedvige', 'Johnson', 45, 'Male', 'China', 'Lebsack Inc');
insert into person (name, first_name, age, gender, country, company) values ('Aleta', 'Lawson', 36, 'Female', 'China', 'Schuster-Brekke');
insert into person (name, first_name, age, gender, country, company) values ('Rikki', 'Leo', 46, 'Male', 'China', 'Dickens Group');
insert into person (name, first_name, age, gender, country, company) values ('Angelica', 'Ronald', 20, 'Male', 'China', 'Okuneva and Hansen');
insert into person (name, first_name, age, gender, country, company) values ('Sollie', 'Gigi', 46, 'Female', 'China', 'Hoppe-Kunde');
insert into person (name, first_name, age, gender, country, company) values ('Somerset', 'Leslie', 49, 'Male', 'China', 'Doyle Inc');
insert into person (name, first_name, age, gender, country, company) values ('Florence', 'Fillip', 26, 'Male', 'Russia', 'Hane LLC');
insert into person (name, first_name, age, gender, country, company) values ('Meriel', 'Rick', 34, 'Male', 'Russia', 'Goodwin and Ledner');
insert into person (name, first_name, age, gender, country, company) values ('Elaine', 'Dick', 29, 'Male', 'China', 'Mann-McCullough');
insert into person (name, first_name, age, gender, country, company) values ('Muhammad', 'Ruff', 45, 'Female', 'China', 'Hane and Sons');
insert into person (name, first_name, age, gender, country, company) values ('Cletus', 'Charles', 43, 'Male', 'China', 'Schroeder Group Ardra');
insert into person (name, first_name, age, gender, country, company) values ('Elton', 'David', 46, 'Male', 'China', 'Funk-Homenick');
insert into person (name, first_name, age, gender, country, company) values ('Wainwright', 'Cherry', 23, 'Female', 'China', 'Lindgren');
insert into person (name, first_name, age, gender, country, company) values ('Kaspar', 'Linda', 38, 'Female', 'China', 'Lynch-Torp');
insert into person (name, first_name, age, gender, country, company) values ('Ardra', 'Jane', 25, 'Female', 'China', 'McCullough and Brakus');
insert into person (name, first_name, age, gender, country, company) values ('Dall', 'Bill', 29, 'Female', 'China', 'Crona and Sons');
insert into person (name, first_name, age, gender, country, company) values ('Ruthy', 'Mike', 35, 'Male', 'China', 'Ondricka-Jacobi');
insert into person (name, first_name, age, gender, country, company) values ('Averil', 'Phoebe', 39, 'Female', 'China', 'Tillman and Ortiz');