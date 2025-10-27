# UniFix - University Facilities Management System

UniFix is a comprehensive desktop application designed to streamline the management of maintenance requests and facility issues in university hostels and campus buildings. The system provides a centralized platform for students, wardens, technicians, and administrators to handle maintenance complaints efficiently.

![UniFix Logo](resources/icons/logo.png)

## Key Features

### For Students
- **User-friendly Dashboard**: Easy-to-navigate interface showing complaint statistics and quick actions
- **Submit Complaints**: Report maintenance issues with descriptions, locations, and optional images
- **Track Requests**: Monitor the status of submitted complaints in real-time
- **View Solutions**: Access solutions provided by technicians for resolved issues
- **Profile Management**: Update personal information and contact details

### For Technicians
- **Assigned Tasks View**: See all maintenance tasks assigned to them
- **Status Updates**: Change complaint status (pending, in progress, resolved)
- **Solution Documentation**: Record resolution details for knowledge sharing
- **History Tracking**: Access past maintenance records

### For Wardens/Administrators
- **Oversight Dashboard**: View all complaints across buildings/hostels
- **Assignment Capabilities**: Assign complaints to appropriate technicians
- **Analytics**: View statistics on complaint types, resolution times, and common issues
- **User Management**: Add/modify user accounts and permissions

## Technologies Used
- **Java Swing**: For the desktop application GUI
- **MySQL**: Database backend for storing user data, complaints, and solutions
- **JDBC**: For database connectivity
- **Java AWT**: For graphical components and custom UI elements

## System Requirements
- Java Runtime Environment (JRE) 8 or higher
- MySQL 5.7 or higher
- Minimum 4GB RAM
- 100MB free disk space

## Installation and Setup

### Prerequisites
1. Install Java JDK/JRE 8 or higher
2. Install MySQL server
3. Create a database named 'unifix'

### Database Configuration
1. Edit the `db_config.properties` file with your database credentials:
   ```
   db.url=jdbc:mysql://localhost:3306/unifix
   db.user=your_username
   db.password=your_password
   ```

### First-Time Setup
1. Clone the repository:
   ```
   git clone https://github.com/yourusername/unifix.git
   ```
2. Navigate to the project directory:
   ```
   cd unifix
   ```
3. Run the setup script:
   - Windows: `run_setup.bat`
   - Linux/Mac: `./run_setup.sh`

### Running the Application
- Windows: Double-click on `run.bat`
- Linux/Mac: Execute `./run.sh`

## User Guide

### Login
The application provides role-based access through a secure login system. Different interfaces are shown based on user roles:
- Student
- Technician
- Warden
- Administrator

### Student Workflow
1. Login with student credentials
2. View dashboard statistics
3. Click "New Complaint" to report an issue
4. Fill out the form with details (location, category, description, optional image)
5. Submit and track progress in "My Complaints"

### Technical Troubleshooting
- **Database Connection Issues**: Check `db_config.properties` file and ensure MySQL server is running
- **Image Upload Problems**: Verify that the 'uploads' directory has write permissions
- **UI Rendering Issues**: Update your Java version to the latest release

## Project Structure
```
unifix/
├── bin/                    # Compiled Java classes
├── lib/                    # External libraries
├── resources/              # Application resources (icons, images)
├── sql/                    # SQL scripts for database setup
├── src/                    # Source code
│   └── com/unifix/
│       ├── auth/           # Authentication components
│       ├── complaints/     # Complaint management
│       ├── dashboard/      # User interfaces
│       ├── database/       # Database connectivity
│       ├── main/           # Application entry points
│       ├── solutions/      # Solution management
│       └── utils/          # Utility classes
├── uploads/                # User uploaded content
├── db_config.properties    # Database configuration
├── run.bat                 # Windows startup script
└── README.md               # Project documentation
```

## Development and Contribution

### Setup Development Environment
1. Install a Java IDE (Eclipse, IntelliJ IDEA, or NetBeans)
2. Import the project as a Java project
3. Configure the build path to include JARs from the 'lib' directory
4. Use the provided run configuration or create a new one with the main class: `com.unifix.main.UniFix`

### Coding Standards
- Follow Java naming conventions
- Add appropriate comments for complex logic
- Create JUnit tests for new features

## License
This project is licensed under the MIT License - see the LICENSE file for details.

## Contributors
- [Your Name](https://github.com/yourusername)
- [Contributor Name](https://github.com/contributorname)

## Acknowledgments
- University IT Department for requirements specification
- Student Council for user testing and feedback
- Faculty mentors for project guidance

---

© 2025 UniFix Team. All rights reserved.
