# eDocs_OCR_Spring
This is the **eDocs OCR Scheduler**, a service built with Spring Boot.

## Prerequisites

Before running this service locally, ensure you have the required dependencies installed on your machine.

---

## OCRMyPdf Installation Steps

### For Windows

1. **Install Python (Latest Version):**
   - Download and install Python (e.g., `python-3.11.0a1-amd64.exe`).
   - Verify installation:  
     ```bash
     python --version
     ```  
     Example output: `3.9.6`

2. **Install Ghostscript (Latest Version):**
   - Download and install Ghostscript (e.g., `gs9550w64.exe`).

3. **Install Tesseract-OCR (Latest Version):**
   - Download and install Tesseract OCR (e.g., `tesseract-ocr-w64-setup-v5.0.0-alpha.20210506.exe`).

4. **Install Required Python Libraries:**
   - Open Command Prompt **(Run as Administrator)**.
   - Run the following commands one by one:
     ```bash
     pip install pypdfocr
     pip install Pillow
     pip install reportlab
     pip install watchdog
     pip install pypdf2
     pip install matplotlib
     pip install tesseract
     pip install leptonica
     pip install argh
     pip install gtp
     pip install sgf
     pip install pngquant
     ```

5. **Install OCRMyPdf:**
   - Run the following command:
     ```bash
     pip install ocrmypdf
     ```
   - Verify installation:
     ```bash
     ocrmypdf --version
     ```

6. **Set Up Folder Structure:**
   - Copy the `bpo_ocr_home` folder structure to the `C:\` drive.

7. **Test OCR Functionality:**
   - Open Windows PowerShell and run:
     ```bash
     ocrmypdf --force-ocr 1.pdf 1_output.pdf
     ```

---

### For Linux

1. **Install Python (Latest Version):**
   - Verify installation:
     ```bash
     python3 --version
     ```

2. **Install Ghostscript (Latest Version).**

3. **Install Tesseract-OCR (Latest Version).**

4. **Install Required Python Libraries:**
   - Open your terminal or PuTTY.
   - Run the following commands one by one:
     ```bash
     pip3 install pypdfocr
     pip3 install Pillow
     pip3 install reportlab
     pip3 install watchdog
     pip3 install pypdf2
     pip3 install matplotlib
     pip3 install tesseract
     pip3 install leptonica
     pip3 install argh
     pip3 install gtp
     pip3 install sgf
     pip3 install pngquant
     ```

5. **Install OCRMyPdf:**
   - Run the following command:
     ```bash
     pip3 install ocrmypdf
     ```

---

### Notes
- Make sure all required dependencies are installed before running the scheduler.
- Update the folder structure as needed for your environment.

---

Feel free to reach out if you encounter any issues!
