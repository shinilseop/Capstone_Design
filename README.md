# 내 위치의 빛 (캡스톤 디자인 프로젝트)

### Contents

0. [소개](#0-소개)
1. [프로젝트 시작 배경](#1-프로젝트-시작-배경)
2. [프로젝트 목적](#2-프로젝트-목적)
3. [기능 설계](#3-기능-설계)
4. [구현 장면](#4-구현-장면)

---

### 0. 소개
* 저는 공주대학교 공과대학의 컴퓨터공학부 소프트웨어전공 졸업생 신일섭입니다.
* 본 프로젝트는 캡스톤 디자인 프로젝트를 진행하면서 제작했던 어플리케이션입니다.

---

### 1. 프로젝트 시작 배경
* 실외의 빛 (자연광)

<p align="center"><img src="/readme_image/background1.png"  width="80%" height="80%"/></p>

* 실내의 빛 (조명)

<p align="center"><img src="/readme_image/background2.png"  width="80%" height="80%"/></p>

---

### 2. 프로젝트 목적
* 목적

<p align="center"><img src="/readme_image/obj1.png"  width="80%" height="80%"/></p>

---

### 3. 기능 설계
* 기능도

<p align="center"><img src="/readme_image/func1.png"  width="80%" height="80%"/></p>
   
   
   * Python의 Keras 패키지를 이용하여 조도 기반의 자외선 산출을 하는 DNN 모델을 개발
   * 모델을 안드로이드에서 실행하기위해 tensorflow-lite 모델로 변환
   * 사용자에게 서비스 하기 위해서 안드로이드 어플리케이션 개발
   * 스마트폰의 내부 센서인 조도 센서와 컬러 센서를 이용하여 조도와 측정한 RGB를 통한 색온도 산출
   * 측정된 조도는 개발한 모델에 적용 시켜 자외선 산출
   * 측정된 수치들을 기반으로 비타민D 합성량, 홍반 발생여부 등을 추가적으로 제공
   * 조명의 빛일때를 가정하여 조도와 색온도를 크루이토프 곡선에 적용하여 현재 조명의 상태 제공
   
---

### 4. 구현 장면

* 실행 장면 1

<p align="center"><img src="/readme_image/run1.png"  width="80%" height="80%"/></p>

* 실행 장면 2

<p align="center"><img src="/readme_image/run2.png"  width="80%" height="80%"/></p>

* 실행 장면 3

<p align="center"><img src="/readme_image/run3.png"  width="80%" height="80%"/></p>

* 실행 장면 4

<p align="center"><img src="/readme_image/run4.png"  width="80%" height="80%"/></p>

* 실행 영상

<p align="center"><img src="/readme_image/run_video.gif"  width="25%" height="25%"/></p>

   * 맨 첫번째 기상화면은 측정한 자외선 수치의 비교를 위해 제공하고 있으며, 웨더아이(https://www.weatheri.co.kr/)에서 날씨 정보를 크롤링 하고, 자외선 정보의 경우 실제 공주대학교 10공학관에서 측정 및 스트리밍하는 데이터를 제공함.
