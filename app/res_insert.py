# coding=utf-8
import os
import random
from xml.etree import ElementTree as ET

# 指定Android项目目录
project_dir = os.path.dirname(os.path.abspath(__file__))


def add_element_or_whitespace(file_path):
    # 解析xml文件
    tree = ET.parse(file_path)
    root = tree.getroot()

    if root.tag == 'ViewGroup' \
            or root.tag == 'androidx.constraintlayout.widget.ConstraintLayout' \
            or root.tag == 'LinearLayout' \
            or root.tag == 'RelativeLayout' \
            or root.tag == 'LinearLayout' \
            :
        # 添加新的元素
        new_element = ET.Element('View')
        new_element.set('{http://schemas.android.com/apk/res/android}layout_width',
                        '%ddp' % (random.randint(0, 50)))
        new_element.set('{http://schemas.android.com/apk/res/android}layout_height',
                        '%ddp' % (random.randint(0, 50)))
        new_element.set('{http://schemas.android.com/apk/res/android}visibility', 'gone')
        root.append(new_element)
    else:
        # 在根标签中随机添加空格或回车
        i = random.randint(1, 50)
        while i > 0:
            root.text = root.text + random.choice([' ', '\n'])
            i = i - 1

    ET.register_namespace("android", "http://schemas.android.com/apk/res/android")
    ET.register_namespace("app", "http://schemas.android.com/apk/res-auto")
    ET.register_namespace("tools", "http://schemas.android.com/tools")
    # 保存修改后的xml文件
    tree.write(file_path)


# 遍历项目目录下的所有layout文件夹
for root_dir, sub_dirs, files in os.walk(project_dir):
    if 'layout' in root_dir:
        for file in files:
            if file.endswith('.xml'):
                file_path = os.path.join(root_dir, file)
                add_element_or_whitespace(file_path)
