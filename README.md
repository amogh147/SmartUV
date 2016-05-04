# Smart UV

Ubiquitous application designed to track the user's sun exposure based on the time the person is outdoor.

## Functioning Features

### 1st tab

Display current status:

* Current UV Index
* Colored Risk Level {None(blue), Low(green), Moderate(yellow), High(orange), VeryHigh(red), Extreme(purple)}
* Date and Locality
* Protection Images (hat, shirt, sunscreen, glasses, shade)
* Protection message (text)
* Alert sign for VeryHigh and Extreme

### 2nd tab

Display estimated sun exposure time:

* Tracking button: activate MainService. It may appear activated if the service is already running. (known functional bug: if the service kills itself and schedule the time to start on the next day, the user can't disable it until the next period of interest starts)

* IOState: show the Indoor/Outdoor state (for debug purposes)

* Table: show the UV Index and estimated sun exposure time of the the day for each hour. It automatically updates when the MainService process more data. If the service it is not available, it  reads from the database.

* Date: although there is a spinner to select the date it is not implemented yet. In the future, it is suppose to allow the user to se UV exposure of past days.

### 3rd tab

Only additional information. It has two buttons that redirects the user to related websites

## Bugs

The code still has some bugs that may crash the app. 2 of them are:
* Image rendering problem (something related to memory allocation). The images are not loaded correctly. This is a minor error that does not happens all the time and may take some time to fix, so we didn't pay attention on it;
* Problems in requesting the UV Index update (it seems internet communication problem). I think we should deal with exceptions.


## Motivation

Final project for CS528-S16 course at Worcester Polytechnic Institute (WPI) conducted by Prof. Emmanuel Agu.

Developed by Mateus Amarante Araujo and Amogh Raghunath.

