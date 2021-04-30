create table usertable (
    uname varchar(20) primary key,
    passwd varchar(20),
    tokenid number unique
);

create table userlogin (
    udate date,
    uauth number
);


insert into usertable values ('Abishek','@b#i',001);
insert into usertable values ('Shashank','$shas#',002);
insert into usertable values ('Sujai','$uj2i',003);
insert into usertable values ('Rahul','r@#ul',004);
insert into usertable values ('Sanjay','$anj@y',005);
insert into usertable values ('Athithya','@t#i',006);
insert into usertable values ('Ramesh','r@mes#',007);
insert into usertable values ('Alan','w@lker',008);
insert into usertable values ('Ashwin','@us#win',009);

create or replace trigger datechange
before insert on userlogin
for each row 
when (uauth = 1)
declare
    newdate date;
begin
        newdate := sysdate();
end;
/


create or replace trigger passwordLength
before insert on usertable
for each row
declare
	passwd_v varchar(20),
	insufficient_password_length exception
begin
	if char_length(new.pass) < 8 then
		raise insufficient_password_length;
	end if;
end;
/
